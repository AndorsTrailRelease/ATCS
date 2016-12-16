package com.gpl.rpg.atcontentstudio.model;

import java.awt.Image;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.io.JsonPrettyWriter;
import com.gpl.rpg.atcontentstudio.io.SettingsSave;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.gamedata.ActorCondition;
import com.gpl.rpg.atcontentstudio.model.gamedata.Dialogue;
import com.gpl.rpg.atcontentstudio.model.gamedata.Droplist;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataCategory;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.model.gamedata.Item;
import com.gpl.rpg.atcontentstudio.model.gamedata.ItemCategory;
import com.gpl.rpg.atcontentstudio.model.gamedata.JSONElement;
import com.gpl.rpg.atcontentstudio.model.gamedata.NPC;
import com.gpl.rpg.atcontentstudio.model.gamedata.Quest;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMapSet;
import com.gpl.rpg.atcontentstudio.model.maps.Worldmap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.saves.SavedGamesSet;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.ui.WorkerDialog;
import com.gpl.rpg.atcontentstudio.utils.FileUtils;

public class Project implements ProjectTreeNode, Serializable {

	private static final long serialVersionUID = 4807454973303366758L;

	//Every instance field that is not transient will be saved in this file.
	public static final String SETTINGS_FILE = ".project";
	
	public String name;

	public File baseFolder;
	public boolean open;
	
	public GameSource baseContent; //A.k.a library
	
	public GameSource referencedContent; //Pointers to base content
	public transient GameSource alteredContent; //Copied from base content (does not overwrite yet)
	public transient GameSource createdContent; //Stand-alone.
	
	public SavedGamesSet saves; //For simulations.
	
	public transient SavedSlotCollection v;

	public transient Workspace parent;
	
	public Properties knownSpritesheetsProperties = null;
	
	public static enum ResourceSet {
		gameData,
		debugData,
		allFiles
	} 
	
	public ResourceSet sourceSetToUse = ResourceSet.allFiles;

	public Project(Workspace w, String name, File source, ResourceSet sourceSet) {
		this.parent = w;
		this.name = name;
		this.sourceSetToUse = sourceSet;
		
		//CREATE PROJECT
		baseFolder = new File(w.baseFolder, name+File.separator);
		try {
			baseFolder.mkdir();
		} catch (SecurityException e) {
			Notification.addError("Eror creating project root folder: "+e.getMessage());
			e.printStackTrace();
		}
		open = true;
		v = new SavedSlotCollection();
		
		knownSpritesheetsProperties = new Properties();
		try {
			knownSpritesheetsProperties.load(Project.class.getResourceAsStream("/spritesheets.properties"));
		} catch (IOException e) {
			Notification.addWarn("Unable to load default spritesheets properties.");
			e.printStackTrace();
		}
		
		
		baseContent = new GameSource(source, this);
		
//		referencedContent = new GameSource(this, GameSource.Type.referenced);

		alteredContent = new GameSource(this, GameSource.Type.altered);
		createdContent = new GameSource(this, GameSource.Type.created);
		
		saves = new SavedGamesSet(this);

		v.add(createdContent);
		v.add(alteredContent);
//		v.add(referencedContent);
		v.add(baseContent);
		v.add(saves);

		linkAll();
		
		save();
	}
	
	
	@Override
	public TreeNode getChildAt(int childIndex) {
		return v.getNonEmptyElementAt(childIndex);
	}

	@Override
	public int getChildCount() {
		return v.getNonEmptySize();
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		return v.getNonEmptyIndexOf((ProjectTreeNode) node);
	}

	@Override
	public boolean getAllowsChildren() {
		return open;
	}

	@Override
	public boolean isLeaf() {
		return !open;
	}

	@Override
	public Enumeration<ProjectTreeNode> children() {
		return v.getNonEmptyElements();
	}

	@Override
	public void childrenAdded(List<ProjectTreeNode> path) {
		path.add(0,this);
		parent.childrenAdded(path);
	}

	@Override
	public void childrenChanged(List<ProjectTreeNode> path) {
		path.add(0,this);
		parent.childrenChanged(path);
	}

	@Override
	public void childrenRemoved(List<ProjectTreeNode> path) {
		path.add(0,this);
		parent.childrenRemoved(path);
	}
	@Override
	public void notifyCreated() {
		childrenAdded(new ArrayList<ProjectTreeNode>());
		for (ProjectTreeNode node : v.getNonEmptyIterable()) {
			node.notifyCreated();
		}
	}
	@Override
	public String getDesc() {
		return name;
	}


	public void close() {
		this.open = false;
		childrenRemoved(new ArrayList<ProjectTreeNode>());
	}

	public void open() {
		open = true;
	}


	public static Project fromFolder(Workspace w, File projRoot) {
		Project p = null;
		File f = new File(projRoot, Project.SETTINGS_FILE);
		if (!f.exists()) {
			Notification.addError("Unable to find "+SETTINGS_FILE+" for project "+projRoot.getName());
			return null;
		} else {
			p = (Project) SettingsSave.loadInstance(f, "Project");
		}
		p.refreshTransients(w);
		return p;
	}
	
	public void refreshTransients(Workspace w) {
		this.parent = w;

		try {
			knownSpritesheetsProperties = new Properties();
			knownSpritesheetsProperties.load(Project.class.getResourceAsStream("/spritesheets.properties"));
		} catch (IOException e) {
			Notification.addWarn("Unable to load default spritesheets properties.");
			e.printStackTrace();
		}
		
		if (sourceSetToUse == null) {
			sourceSetToUse = ResourceSet.allFiles;
		}
		
//		long l = new Date().getTime();
		baseContent.refreshTransients(this);
//		l = new Date().getTime() - l;
//		System.out.println("All initialized in "+l+"ms.");
//		referencedContent.refreshTransients(this);
		alteredContent = new GameSource(this, GameSource.Type.altered);
		createdContent = new GameSource(this, GameSource.Type.created);
		
		saves.refreshTransients();

		v = new SavedSlotCollection();
		v.add(createdContent);
		v.add(alteredContent);
//		v.add(referencedContent);
		v.add(baseContent);
		v.add(saves);
		

		linkAll();
		
		projectElementListeners = new HashMap<Class<? extends GameDataElement>, List<ProjectElementListener>>();
	}
	
	public void linkAll() {
		for (ProjectTreeNode node : baseContent.gameData.v.getNonEmptyIterable()) {
			if (node instanceof GameDataCategory<?>) {
				for (GameDataElement e : ((GameDataCategory<?>) node)) {
					e.link();
				}
			}
		}
		for (ProjectTreeNode node : baseContent.gameMaps.tmxMaps) {
			((TMXMap)node).parse();
		}
		for (ProjectTreeNode node : alteredContent.gameData.v.getNonEmptyIterable()) {
			if (node instanceof GameDataCategory<?>) {
				for (GameDataElement e : ((GameDataCategory<?>) node)) {
					e.link();
				}
			}
		}
		for (ProjectTreeNode node : alteredContent.gameMaps.tmxMaps) {
			((TMXMap)node).parse();
		}
		for (ProjectTreeNode node : createdContent.gameData.v.getNonEmptyIterable()) {
			if (node instanceof GameDataCategory<?>) {
				for (GameDataElement e : ((GameDataCategory<?>) node)) {
					e.link();
				}
			}
		}
		for (ProjectTreeNode node : createdContent.gameMaps.tmxMaps) {
			((TMXMap)node).parse();
		}
		for (ProjectTreeNode node : baseContent.gameMaps.tmxMaps) {
			((TMXMap)node).link();
		}
		
		for (WorldmapSegment node : createdContent.worldmap) {
			node.link();
		}
		for (WorldmapSegment node : alteredContent.worldmap) {
			node.link();
		}
		for (WorldmapSegment node : baseContent.worldmap) {
			node.link();
		}
	}
	
	public void save() {
		SettingsSave.saveInstance(this, new File(baseFolder, Project.SETTINGS_FILE), "Project "+this.name);
	}
	
	
	public JSONElement getGameDataElement(Class<? extends JSONElement> gdeClass, String id) {
		if (gdeClass == ActorCondition.class) {
			return getActorCondition(id);
		}
		if (gdeClass == Dialogue.class) {
			return getDialogue(id);
		}
		if (gdeClass == Droplist.class) {
			return getDroplist(id);
		}
		if (gdeClass == ItemCategory.class) {
			return getItemCategory(id);
		}
		if (gdeClass == Item.class) {
			return getItem(id);
		}
		if (gdeClass == NPC.class) {
			return getNPC(id);
		}
		if (gdeClass == Quest.class) {
			return getQuest(id);
		}
		return null;
	}
	
	public int getNodeCount(Class<? extends GameDataElement> gdeClass) {
		if (gdeClass == ActorCondition.class) {
			return getActorConditionCount();
		}
		if (gdeClass == Dialogue.class) {
			return getDialogueCount();
		}
		if (gdeClass == Droplist.class) {
			return getDroplistCount();
		}
		if (gdeClass == ItemCategory.class) {
			return getItemCategoryCount();
		}
		if (gdeClass == Item.class) {
			return getItemCount();
		}
		if (gdeClass == NPC.class) {
			return getNPCCount();
		}
		if (gdeClass == Quest.class) {
			return getQuestCount();
		}
		return 0;
	}
	
	public int getNodeIndex(GameDataElement node) {
		Class<? extends GameDataElement> gdeClass = node.getClass();
		if (gdeClass == ActorCondition.class) {
			return getActorConditionIndex((ActorCondition) node);
		}
		if (gdeClass == Dialogue.class) {
			return getDialogueIndex((Dialogue) node);
		}
		if (gdeClass == Droplist.class) {
			return getDroplistIndex((Droplist) node);
		}
		if (gdeClass == ItemCategory.class) {
			return getItemCategoryIndex((ItemCategory) node);
		}
		if (gdeClass == Item.class) {
			return getItemIndex((Item) node);
		}
		if (gdeClass == NPC.class) {
			return getNPCIndex((NPC) node);
		}
		if (gdeClass == Quest.class) {
			return getQuestIndex((Quest) node);
		}
		return 0;
	}

	public ActorCondition getActorCondition(String id) {
		ActorCondition gde = createdContent.gameData.getActorCondition(id);
		if (gde == null) gde = alteredContent.gameData.getActorCondition(id);
		if (gde == null) gde = baseContent.gameData.getActorCondition(id);
		return gde;
	}

	public int getActorConditionCount() {
		return createdContent.gameData.actorConditions.size() + baseContent.gameData.actorConditions.size();
	}

	public ActorCondition getActorCondition(int index) {
		if (index < createdContent.gameData.actorConditions.size()) {
			return createdContent.gameData.actorConditions.get(index);
		} else if (index < getActorConditionCount()){
			return getActorCondition(baseContent.gameData.actorConditions.get(index - createdContent.gameData.actorConditions.size()).id);
		}
		return null;
	}
	
	public int getActorConditionIndex(ActorCondition ac) {
		if (ac.getDataType() == GameSource.Type.created) {
			return createdContent.gameData.actorConditions.getIndex(ac);
		} else {
			return createdContent.gameData.actorConditions.size() + baseContent.gameData.actorConditions.indexOf(baseContent.gameData.getActorCondition(ac.id));
		}
	}

	
	public Dialogue getDialogue(String id) {
		Dialogue gde = createdContent.gameData.getDialogue(id);
		if (gde == null) gde = alteredContent.gameData.getDialogue(id);
		if (gde == null) gde = baseContent.gameData.getDialogue(id);
		return gde;
	}

	public int getDialogueCount() {
		return createdContent.gameData.dialogues.size() + baseContent.gameData.dialogues.size();
	}

	public Dialogue getDialogue(int index) {
		if (index < createdContent.gameData.dialogues.size()) {
			return createdContent.gameData.dialogues.get(index);
		} else if (index < getDialogueCount()){
			return getDialogue(baseContent.gameData.dialogues.get(index - createdContent.gameData.dialogues.size()).id);
		}
		return null;
	}

	public int getDialogueIndex(Dialogue dialogue) {
		if (dialogue.getDataType() == GameSource.Type.created) {
			return createdContent.gameData.dialogues.getIndex(dialogue);
		} else {
			return createdContent.gameData.dialogues.size() + baseContent.gameData.dialogues.indexOf(baseContent.gameData.getDialogue(dialogue.id));
		}
	}

	
	public Droplist getDroplist(String id) {
		Droplist gde = createdContent.gameData.getDroplist(id);
		if (gde == null) gde = alteredContent.gameData.getDroplist(id);
		if (gde == null) gde = baseContent.gameData.getDroplist(id);
		return gde;
	}
	
	public int getDroplistCount() {
		return createdContent.gameData.droplists.size() + baseContent.gameData.droplists.size();
	}

	public Droplist getDroplist(int index) {
		if (index < createdContent.gameData.droplists.size()) {
			return createdContent.gameData.droplists.get(index);
		} else if (index < getDroplistCount()){
			return getDroplist(baseContent.gameData.droplists.get(index - createdContent.gameData.droplists.size()).id);
		}
		return null;
	}
	
	public int getDroplistIndex(Droplist droplist) {
		if (droplist.getDataType() == GameSource.Type.created) {
			return createdContent.gameData.droplists.getIndex(droplist);
		} else {
			return createdContent.gameData.droplists.size() + baseContent.gameData.droplists.indexOf(baseContent.gameData.getDroplist(droplist.id));
		}
	}
	
	
	public Item getItem(String id) {
		Item gde = createdContent.gameData.getItem(id);
		if (gde == null) gde = alteredContent.gameData.getItem(id);
		if (gde == null) gde = baseContent.gameData.getItem(id);
		return gde;
	}

	public int getItemCount() {
		return createdContent.gameData.items.size() + baseContent.gameData.items.size();
	}

	public Item getItem(int index) {
		if (index < createdContent.gameData.items.size()) {
			return createdContent.gameData.items.get(index);
		} else if (index < getItemCount()){
			return getItem(baseContent.gameData.items.get(index - createdContent.gameData.items.size()).id);
		}
		return null;
	}
	
	public int getItemCountIncludingAltered() {
		return createdContent.gameData.items.size() + alteredContent.gameData.items.size() + baseContent.gameData.items.size();
	}

	public Item getItemIncludingAltered(int index) {
		if (index < createdContent.gameData.items.size()) {
			return createdContent.gameData.items.get(index);
		} else if (index < createdContent.gameData.items.size() + alteredContent.gameData.items.size()){
			return alteredContent.gameData.items.get(index - createdContent.gameData.items.size());
		} else if (index < getItemCountIncludingAltered()) {
			return baseContent.gameData.items.get(index - (createdContent.gameData.items.size() + alteredContent.gameData.items.size()));
		}
		return null;
	}
	
	public int getItemIndex(Item item) {
		if (item.getDataType() == GameSource.Type.created) {
			return createdContent.gameData.items.getIndex(item);
		} else {
			return createdContent.gameData.items.size() + baseContent.gameData.items.indexOf(baseContent.gameData.getItem(item.id));
		}
	}

	
	public ItemCategory getItemCategory(String id) {
		ItemCategory gde = createdContent.gameData.getItemCategory(id);
		if (gde == null) gde = alteredContent.gameData.getItemCategory(id);
		if (gde == null) gde = baseContent.gameData.getItemCategory(id);
		return gde;
	}
	
	public int getItemCategoryCount() {
		return createdContent.gameData.itemCategories.size() + baseContent.gameData.itemCategories.size();
	}

	public ItemCategory getItemCategory(int index) {
		if (index < createdContent.gameData.itemCategories.size()) {
			return createdContent.gameData.itemCategories.get(index);
		} else if (index < getItemCategoryCount()){
			return getItemCategory(baseContent.gameData.itemCategories.get(index - createdContent.gameData.itemCategories.size()).id);
		}
		return null;
	}
	
	public int getItemCategoryIndex(ItemCategory iCat) {
		if (iCat.getDataType() == GameSource.Type.created) {
			return createdContent.gameData.itemCategories.getIndex(iCat);
		} else {
			return createdContent.gameData.itemCategories.size() + baseContent.gameData.itemCategories.indexOf(baseContent.gameData.getItemCategory(iCat.id));
		}
	}

	
	public NPC getNPC(String id) {
		NPC gde = createdContent.gameData.getNPC(id);
		if (gde == null) gde = alteredContent.gameData.getNPC(id);
		if (gde == null) gde = baseContent.gameData.getNPC(id);
		return gde;
	}

	public NPC getNPCIgnoreCase(String id) {
		NPC gde = createdContent.gameData.getNPCIgnoreCase(id);
		if (gde == null) gde = alteredContent.gameData.getNPCIgnoreCase(id);
		if (gde == null) gde = baseContent.gameData.getNPCIgnoreCase(id);
		return gde;
	}

	public int getNPCCount() {
		return createdContent.gameData.npcs.size() + baseContent.gameData.npcs.size();
	}

	public NPC getNPC(int index) {
		if (index < createdContent.gameData.npcs.size()) {
			return createdContent.gameData.npcs.get(index);
		} else if (index < getNPCCount()){
			return getNPC(baseContent.gameData.npcs.get(index - createdContent.gameData.npcs.size()).id);
		}
		return null;
	}
	
	public int getNPCCountIncludingAltered() {
		return createdContent.gameData.npcs.size() + alteredContent.gameData.npcs.size() + baseContent.gameData.npcs.size();
	}

	public NPC getNPCIncludingAltered(int index) {
		if (index < createdContent.gameData.npcs.size()) {
			return createdContent.gameData.npcs.get(index);
		} else if (index < createdContent.gameData.npcs.size() + alteredContent.gameData.npcs.size()){
			return alteredContent.gameData.npcs.get(index - createdContent.gameData.npcs.size());
		} else if (index < getNPCCountIncludingAltered()) {
			return baseContent.gameData.npcs.get(index - (createdContent.gameData.npcs.size() + alteredContent.gameData.npcs.size()));
		}
		return null;
	}
	
	public int getNPCIndex(NPC npc) {
		if (npc.getDataType() == GameSource.Type.created) {
			return createdContent.gameData.npcs.getIndex(npc);
		} else {
			return createdContent.gameData.npcs.size() + baseContent.gameData.npcs.indexOf(baseContent.gameData.getNPC(npc.id));
		}
	}
	
	
	public Quest getQuest(String id) {
		Quest gde = createdContent.gameData.getQuest(id);
		if (gde == null) gde = alteredContent.gameData.getQuest(id);
		if (gde == null) gde = baseContent.gameData.getQuest(id);
		return gde;
	}
	
	public int getQuestCount() {
		return createdContent.gameData.quests.size() + baseContent.gameData.quests.size();
	}

	public Quest getQuest(int index) {
		if (index < createdContent.gameData.quests.size()) {
			return createdContent.gameData.quests.get(index);
		} else if (index < getQuestCount()){
			return getQuest(baseContent.gameData.quests.get(index - createdContent.gameData.quests.size()).id);
		}
		return null;
	}
	
	public int getQuestIndex(Quest quest) {
		if (quest.getDataType() == GameSource.Type.created) {
			return createdContent.gameData.quests.getIndex(quest);
		} else {
			return createdContent.gameData.quests.size() + baseContent.gameData.quests.indexOf(baseContent.gameData.getQuest(quest.id));
		}
	}
	
	public WorldmapSegment getWorldmapSegment(String id) {
		WorldmapSegment gde = createdContent.getWorldmapSegment(id);
		if (gde == null) gde = alteredContent.getWorldmapSegment(id);
		if (gde == null) gde = baseContent.getWorldmapSegment(id);
		return gde;
	}
	
	public int getWorldmapSegmentCount() {
		return createdContent.worldmap.size() + baseContent.worldmap.size();
	}

	public WorldmapSegment getWorldmapSegment(int index) {
		if (index < createdContent.worldmap.size()) {
			return createdContent.worldmap.get(index);
		} else if (index < getWorldmapSegmentCount()){
			return getWorldmapSegment(baseContent.worldmap.get(index - createdContent.worldmap.size()).id);
		}
		return null;
	}
	
	public int getWorldmapSegmentIndex(WorldmapSegment segment) {
		if (segment.getDataType() == GameSource.Type.created) {
			return createdContent.worldmap.getIndex(segment);
		} else {
			return createdContent.worldmap.size() + baseContent.worldmap.indexOf(baseContent.getWorldmapSegment(segment.id));
		}
	}
	
	public Image getIcon(String iconId) {
		return baseContent.getIcon(iconId);
	}
	
	public Image getImage(String iconId) {
		return baseContent.getImage(iconId);
	}

	public Spritesheet getSpritesheet(String id) {
		Spritesheet sheet = createdContent.gameSprites.getSpritesheet(id);
		if (sheet == null) sheet = alteredContent.gameSprites.getSpritesheet(id);
		if (sheet == null) sheet = baseContent.gameSprites.getSpritesheet(id);
		return sheet;
	}
	
	
	public TMXMap getMap(String id) {
		TMXMap map = createdContent.gameMaps.getMap(id);
		if (map == null) map = alteredContent.gameMaps.getMap(id);
		if (map == null) map = baseContent.gameMaps.getMap(id);
		return map;
	}
	
	public int getMapCount() {
		return createdContent.gameMaps.getChildCount() + baseContent.gameMaps.getChildCount();
	}

	public TMXMap getMap(int index) {
		if (index < createdContent.gameMaps.getChildCount()) {
			return createdContent.gameMaps.get(index);
		} else if (index < getMapCount()){
			return getMap(baseContent.gameMaps.get(index - createdContent.gameMaps.getChildCount()).id);
		}
		return null;
	}

	public int getMapIndex(TMXMap map) {
		if (map.getDataType() == GameSource.Type.created) {
			return createdContent.gameMaps.tmxMaps.indexOf(map);
		} else {
			return createdContent.gameMaps.tmxMaps.size() + baseContent.gameMaps.tmxMaps.indexOf(baseContent.gameMaps.getMap(map.id));
		}
	}
	
	public int getWriterSketchCount() {
		return createdContent.writerModeDataSet.getChildCount();
	}
	
	public WriterModeData getWriterSketch(String id) {
		return createdContent.writerModeDataSet.getWriterSketch(id);
	}
	
	public WriterModeData getWriterSketch(int index) {
		if (index < createdContent.writerModeDataSet.getChildCount()) {
			return createdContent.writerModeDataSet.get(index);
		} 
		return null;
	}

	@Override
	public Project getProject() {
		return this;
	}

	@Override
	public Image getIcon() {
		return getOpenIcon();
	}
	@Override
	public Image getClosedIcon() {
		//TODO Create a cool Project icon.
		return DefaultIcons.getStdClosedIcon();
	}
	@Override
	public Image getLeafIcon() {
		//TODO Create a cool Project icon.
		return DefaultIcons.getStdClosedIcon();
	}
	@Override
	public Image getOpenIcon() {
		//TODO Create a cool Project icon.
		return DefaultIcons.getStdOpenIcon();
	}
	
	public void makeWritable(JSONElement node) {
		GameSource.Type type = node.getDataType();
		if (type == null) {
			Notification.addError("Unable to make "+node.getDesc()+" writable. No owning GameDataSet found.");
		} else {
			if (type == GameSource.Type.source) {
				JSONElement clone = (JSONElement) node.clone();
				for (GameDataElement backlink : node.getBacklinks()) {
					backlink.elementChanged(node, clone);
				}
				node.getBacklinks().clear();
				clone.writable = true;
				clone.state = GameDataElement.State.created;
				alteredContent.gameData.addElement(clone);
			} else {
				Notification.addError("Unable to make "+node.getDesc()+" writable. It does not originate from game source material.");
			}
		}
	}
	


	public void makeWritable(TMXMap node) {
		GameSource.Type type = node.getDataType();
		if (type == null) {
			Notification.addError("Unable to make "+node.getDesc()+" writable. No owning GameDataSet found.");
		} else {
			if (type == GameSource.Type.source) {
				TMXMap clone = node.clone();
				for (GameDataElement backlink : node.getBacklinks()) {
					backlink.elementChanged(node, clone);
				}
				node.getBacklinks().clear();
				clone.writable = true;
				clone.state = GameDataElement.State.created;
				alteredContent.gameMaps.addMap(clone);
			} else {
				Notification.addError("Unable to make "+node.getDesc()+" writable. It does not originate from game source material.");
			}
		}
	}

	public void makeWritable(WorldmapSegment node) {
		GameSource.Type type = node.getDataType();
		if (type == null) {
			Notification.addError("Unable to make "+node.getDesc()+" writable. No owning GameDataSet found.");
		} else {
			if (type == GameSource.Type.source) {
				WorldmapSegment clone = node.clone();
				for (GameDataElement backlink : node.getBacklinks()) {
					backlink.elementChanged(node, clone);
				}
				clone.state = GameDataElement.State.init;
				clone.parse();
				node.getBacklinks().clear();
				clone.writable = true;
				clone.state = GameDataElement.State.created;
				alteredContent.worldmap.addSegment(clone);
			} else {
				Notification.addError("Unable to make "+node.getDesc()+" writable. It does not originate from game source material.");
			}
		}
	}
	
	/**
	 * 
	 * @param node. Before calling this method, make sure that no other node with the same class and id exist in either created or altered.
	 */
	public void createElement(JSONElement node) {
		node.writable = true;
		if (getGameDataElement(node.getClass(), node.id) != null) {
			GameDataElement existingNode = getGameDataElement(node.getClass(), node.id);
			for (GameDataElement backlink : existingNode.getBacklinks()) {
				backlink.elementChanged(existingNode, node);
			}
			existingNode.getBacklinks().clear();
			node.writable = true;
			alteredContent.gameData.addElement(node);
			node.link();
			node.state = GameDataElement.State.created;
		} else {
			createdContent.gameData.addElement(node);
			node.link();
			node.state =  GameDataElement.State.created;
		}
		fireElementAdded(node, getNodeIndex(node));
	}
	
	/**
	 * 
	 * @param node. Before calling this method, make sure that no other node with the same class and id exist in either created or altered.
	 */
	public void createElements(List<JSONElement> nodes) {
		for (JSONElement node : nodes) {
			//Already added.
			if (node.getProject() != null) continue;
			node.writable = true;
			if (getGameDataElement(node.getClass(), node.id) != null) {
				GameDataElement existingNode = getGameDataElement(node.getClass(), node.id);
				for (GameDataElement backlink : existingNode.getBacklinks()) {
					backlink.elementChanged(existingNode, node);
				}
				existingNode.getBacklinks().clear();
				node.writable = true;
				alteredContent.gameData.addElement(node);
			} else {
				createdContent.gameData.addElement(node);
			}
		}
		for (JSONElement node : nodes) {
			node.link();
			node.state =  GameDataElement.State.created;
			fireElementAdded(node, getNodeIndex(node));
		}
	}
	

	public void moveToCreated(JSONElement target) {
		target.childrenRemoved(new ArrayList<ProjectTreeNode>());
		((GameDataCategory<?>)target.getParent()).remove(target);
		target.state = GameDataElement.State.created;
		createdContent.gameData.addElement(target);
	}
	
	public void moveToAltered(JSONElement target) {
		target.childrenRemoved(new ArrayList<ProjectTreeNode>());
		((GameDataCategory<?>)target.getParent()).remove(target);
		target.state = GameDataElement.State.created;
		((JSONElement) target).jsonFile = new File(baseContent.gameData.getGameDataElement(((JSONElement)target).getClass(), target.id).jsonFile.getAbsolutePath());
		alteredContent.gameData.addElement((JSONElement) target);
	}
	
	public void createWorldmapSegment(WorldmapSegment node) {
		node.writable = true;
		if (getWorldmapSegment(node.id) != null) {
			WorldmapSegment existingNode = getWorldmapSegment(node.id);
			for (GameDataElement backlink : existingNode.getBacklinks()) {
				backlink.elementChanged(existingNode, node);
			}
			existingNode.getBacklinks().clear();
			node.writable = true;
			node.state = GameDataElement.State.created;
			alteredContent.worldmap.addSegment(node);
			node.link();
		} else {
			createdContent.worldmap.addSegment(node);
			node.state =  GameDataElement.State.created;
			node.link();
		}
		fireElementAdded(node, getNodeIndex(node));
	}



	public void createWriterSketch(WriterModeData node) {
		node.writable = true;
		createdContent.writerModeDataSet.add(node);
		node.state =  GameDataElement.State.created;
		node.link();
		fireElementAdded(node, getNodeIndex(node));
	}
	
	
	@Override
	public GameDataSet getDataSet() {
		return null;
	}

	@Override
	public Type getDataType() {
		return null;
	}
	
	
	public String getSpritesheetsProperty(String string) {
		return knownSpritesheetsProperties.getProperty(string);
	}

	public void setSpritesheetsProperty(String key, String value) {
		knownSpritesheetsProperties.setProperty(key, value);
	}


	@Override
	public boolean isEmpty() {
		return v.isEmpty();
	}


	public void addSave(File selectedFile) {
		saves.addSave(selectedFile);
	}


	public List<NPC> getSpawnGroup(String spawngroup_id) {
		List<NPC> result = new ArrayList<NPC>();
		int i = getNPCCount();
		boolean alreadyAdded = false;
		int index = -1;
		while (--i >= 0) {
			NPC npc = getNPC(i);
			if (spawngroup_id.equalsIgnoreCase(npc.spawngroup_id)) {
				for (NPC present : result) {
					if (present.id.equals(npc.id)) {
						alreadyAdded = true;
						index = result.indexOf(present);
						break;
					}
				}
				if (alreadyAdded) {
					result.set(index, npc);
				} else {
					result.add(npc);
				}
			}
			alreadyAdded = false;
		}
		if (result.isEmpty()) {
			//Fallback case. A single NPC does not declare a spawn group, but is referred by its ID in maps' spawn areas.
			NPC npc = getNPCIgnoreCase(spawngroup_id);
			if (npc != null) result.add(npc);
		}
		return result;
	}
	
	transient Map<Class<? extends GameDataElement>, List<ProjectElementListener>> projectElementListeners = new HashMap<Class<? extends GameDataElement>, List<ProjectElementListener>>();
	
	public void addElementListener(Class<? extends GameDataElement> interestingType, ProjectElementListener listener) {
		if (projectElementListeners.get(interestingType) == null) {
			projectElementListeners.put(interestingType, new ArrayList<ProjectElementListener>());
		}
		projectElementListeners.get(interestingType).add(listener);
	}
	
	public void removeElementListener(Class<? extends GameDataElement> interestingType, ProjectElementListener listener) {
		if (projectElementListeners.get(interestingType) != null) projectElementListeners.get(interestingType).remove(listener);
	}
	
	public void fireElementAdded(GameDataElement element, int index) {
		if (projectElementListeners.get(element.getClass()) != null) {
			for (ProjectElementListener l : projectElementListeners.get(element.getClass())) {
				l.elementAdded(element, index);
			}
		}
	}

	public void fireElementRemoved(GameDataElement element, int index) {
		if (projectElementListeners.get(element.getClass()) != null) {
			for (ProjectElementListener l : projectElementListeners.get(element.getClass())) {
				l.elementRemoved(element, index);
			}
		}
	}
	
	public void generateExportPackage(final File target) {
		WorkerDialog.showTaskMessage("Exporting project "+name+"...", ATContentStudio.frame, true, new Runnable() {
			@Override
			public void run() {
				Notification.addInfo("Exporting project \""+name+"\" as "+target.getAbsolutePath());
				File tmpDir = new File(baseFolder, "tmp");
				FileUtils.deleteDir(tmpDir);
				tmpDir.mkdir();
				File tmpJsonDataDir = new File(tmpDir, GameDataSet.DEFAULT_REL_PATH_IN_SOURCE);
				tmpJsonDataDir.mkdirs();

				for (File createdJsonFile : createdContent.gameData.baseFolder.listFiles()) {
					FileUtils.copyFile(createdJsonFile, new File(tmpJsonDataDir, createdJsonFile.getName()));
				}
				writeAltered(alteredContent.gameData.actorConditions, baseContent.gameData.actorConditions, ActorCondition.class, tmpJsonDataDir);
				writeAltered(alteredContent.gameData.dialogues, baseContent.gameData.dialogues, Dialogue.class, tmpJsonDataDir);
				writeAltered(alteredContent.gameData.droplists, baseContent.gameData.droplists, Droplist.class, tmpJsonDataDir);
				writeAltered(alteredContent.gameData.itemCategories, baseContent.gameData.itemCategories, ItemCategory.class, tmpJsonDataDir);
				writeAltered(alteredContent.gameData.items, baseContent.gameData.items, Item.class, tmpJsonDataDir);
				writeAltered(alteredContent.gameData.npcs, baseContent.gameData.npcs, NPC.class, tmpJsonDataDir);
				writeAltered(alteredContent.gameData.quests, baseContent.gameData.quests, Quest.class, tmpJsonDataDir);

				File tmpMapDir = new File(tmpDir, TMXMapSet.DEFAULT_REL_PATH_IN_SOURCE);
				tmpMapDir.mkdirs();
				for (File createdMapFile : createdContent.gameMaps.mapFolder.listFiles()) {
					FileUtils.copyFile(createdMapFile, new File(tmpMapDir, createdMapFile.getName()));
				}
				for (File alteredMapFile : alteredContent.gameMaps.mapFolder.listFiles()) {
					FileUtils.copyFile(alteredMapFile, new File(tmpMapDir, alteredMapFile.getName()));
				}
				
				
				if (!createdContent.worldmap.isEmpty() || !alteredContent.worldmap.isEmpty()) {
					try {
						Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
						doc.setXmlVersion("1.0");
						Element root = doc.createElement("worldmap");
						doc.appendChild(root);

						for (int i = 0; i < getWorldmapSegmentCount(); i++) {
							root.appendChild(getWorldmapSegment(i).toXmlElement(doc));
						}

						Worldmap.saveDocToFile(doc, new File(tmpMapDir, "worldmap.xml"));
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				FileUtils.writeToZip(tmpDir, target);
				FileUtils.deleteDir(tmpDir);
				Notification.addSuccess("Project \""+name+"\" exported as "+target.getAbsolutePath());
			}
		});
	}
	
	@SuppressWarnings("rawtypes")
	public void writeAltered(GameDataCategory<? extends JSONElement> altered, GameDataCategory<? extends JSONElement> source, Class<? extends JSONElement> gdeClass, File targetFolder) {
		Set<String> alteredFileNames = new LinkedHashSet<String>();
		Map<String, List<Map>> toWrite = new LinkedHashMap<String, List<Map>>();
		for (JSONElement gde : altered) {
			alteredFileNames.add(gde.jsonFile.getName());
		}
		for (String fName : alteredFileNames) {
			for (JSONElement gde : source) {
				if (gde.jsonFile.getName().equals(fName)) {
					if (toWrite.get(fName) == null) {
						toWrite.put(fName, new ArrayList<Map>());
					}
					toWrite.get(fName).add(getGameDataElement(gdeClass, gde.id).toJson());
				}
			}
		}
		for (String fName : toWrite.keySet()) {
			File jsonFile = new File(targetFolder, fName);
			StringWriter writer = new JsonPrettyWriter();
			try {
				JSONArray.writeJSONString(toWrite.get(fName), writer);
			} catch (IOException e) {
				//Impossible with a StringWriter
			}
			String textToWrite = writer.toString();
			try {
				FileWriter w = new FileWriter(jsonFile);
				w.write(textToWrite);
				w.close();
//				Notification.addSuccess("Json file "+jsonFile.getAbsolutePath()+" saved.");
			} catch (IOException e) {
				Notification.addError("Error while writing json file "+jsonFile.getAbsolutePath()+" : "+e.getMessage());
				e.printStackTrace();
			}
		}
	}



	


	
}
