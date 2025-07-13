package com.gpl.rpg.atcontentstudio.ui.tools;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.gamedata.*;
import com.gpl.rpg.atcontentstudio.model.maps.*;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;

import java.util.ArrayList;
import java.util.List;

public class GDEVisitor {

    public static List<GameDataElement> findDependencies(GameDataElement origin, boolean includeSource) {
        List<GameDataElement> visited = new ArrayList<GameDataElement>();
        visit(origin, visited, includeSource);
        return visited;
    }

    private static void visit(GameDataElement element, List<GameDataElement> visited, boolean includeSource) {
        if (element == null) return;
        if (visited.contains(element)) return;
        if (!(includeSource || element.getDataType() != GameSource.Type.source)) return;

        visited.add(element);
        element.link();
        if (element instanceof ActorCondition) {
            visitActorCondition((ActorCondition) element, visited, includeSource);
        } else if (element instanceof Dialogue) {
            visitDialogue((Dialogue) element, visited, includeSource);
        } else if (element instanceof Droplist) {
            visitDroplist((Droplist) element, visited, includeSource);
        } else if (element instanceof Item) {
            visitItem((Item) element, visited, includeSource);
        } else if (element instanceof ItemCategory) {
            visitItemCategory((ItemCategory) element, visited, includeSource);
        } else if (element instanceof NPC) {
            visitNPC((NPC) element, visited, includeSource);
        } else if (element instanceof Quest) {
            visitQuest((Quest) element, visited, includeSource);
        } else if (element instanceof TMXMap) {
            visitTMXMap((TMXMap) element, visited, includeSource);
        } else if (element instanceof Spritesheet) {
            visitSpritesheet((Spritesheet) element, visited, includeSource);
        }

    }

    private static void visitActorCondition(ActorCondition element, List<GameDataElement> visited, boolean includeSource) {
        if (element.icon_id != null)
            visit(element.getProject().getSpritesheet(element.icon_id.split(":")[0]), visited, includeSource);

        for (GameDataElement backlink : element.getBacklinks()) {
            visit(backlink, visited, includeSource);
        }
    }

    private static void visitDialogue(Dialogue element, List<GameDataElement> visited, boolean includeSource) {
        visit(element.switch_to_npc, visited, includeSource);
        if (element.replies != null) {
            for (Dialogue.Reply reply : element.replies) {
                visit(reply.next_phrase, visited, includeSource);
                if (reply.requirements != null) {
                    for (Requirement req : reply.requirements) {
                        visit(req.required_obj, visited, includeSource);
                    }
                }
            }
        }
        if (element.rewards != null) {
            for (Dialogue.Reward reward : element.rewards) {
                visit(reward.reward_obj, visited, includeSource);
                visit(reward.map, visited, includeSource);
            }
        }

        for (GameDataElement backlink : element.getBacklinks()) {
            visit(backlink, visited, includeSource);
        }
    }

    private static void visitDroplist(Droplist element, List<GameDataElement> visited, boolean includeSource) {
        if (element.dropped_items != null) {
            for (Droplist.DroppedItem droppedItem : element.dropped_items) {
                visit(droppedItem.item, visited, includeSource);
            }
        }

        for (GameDataElement backlink : element.getBacklinks()) {
            visit(backlink, visited, includeSource);
        }
    }

    private static void visitItem(Item element, List<GameDataElement> visited, boolean includeSource) {
        visit(element.category, visited, includeSource);
        if (element.icon_id != null)
            visit(element.getProject().getSpritesheet(element.icon_id.split(":")[0]), visited, includeSource);
        if (element.equip_effect != null && element.equip_effect.conditions != null) {
            for (Common.ActorConditionEffect condEffect : element.equip_effect.conditions) {
                visit(condEffect.condition, visited, includeSource);
            }
        }
        if (element.hit_effect != null) {
            if (element.hit_effect.conditions_source != null) {
                for (Common.ActorConditionEffect condEffect : element.hit_effect.conditions_source) {
                    visit(condEffect.condition, visited, includeSource);
                }
            }
            if (element.hit_effect.conditions_target != null) {
                for (Common.ActorConditionEffect condEffect : element.hit_effect.conditions_target) {
                    visit(condEffect.condition, visited, includeSource);
                }
            }
        }


        for (GameDataElement backlink : element.getBacklinks()) {
            visit(backlink, visited, includeSource);
        }
    }

    private static void visitItemCategory(ItemCategory element, List<GameDataElement> visited, boolean includeSource) {
        //Nothing to visit
    }

    private static void visitNPC(NPC element, List<GameDataElement> visited, boolean includeSource) {
        visit(element.dialogue, visited, includeSource);
        visit(element.droplist, visited, includeSource);
        if (element.icon_id != null)
            visit(element.getProject().getSpritesheet(element.icon_id.split(":")[0]), visited, includeSource);
        if (element.hit_effect != null) {
            if (element.hit_effect.conditions_source != null) {
                for (Common.TimedActorConditionEffect condEffect : element.hit_effect.conditions_source) {
                    visit(condEffect.condition, visited, includeSource);
                }
            }
            if (element.hit_effect.conditions_target != null) {
                for (Common.TimedActorConditionEffect condEffect : element.hit_effect.conditions_target) {
                    visit(condEffect.condition, visited, includeSource);
                }
            }
        }


        for (GameDataElement backlink : element.getBacklinks()) {
            visit(backlink, visited, includeSource);
        }
    }

    private static void visitQuest(Quest element, List<GameDataElement> visited, boolean includeSource) {
        //Nothing to visit


        for (GameDataElement backlink : element.getBacklinks()) {
            visit(backlink, visited, includeSource);
        }
    }

    private static void visitTMXMap(TMXMap element, List<GameDataElement> visited, boolean includeSource) {
        // TODO Auto-generated method stub
        if (element.groups != null) {
            for (MapObjectGroup group : element.groups) {
                if (group.mapObjects != null) {
                    for (MapObject obj : group.mapObjects) {
                        if (obj instanceof ContainerArea) {
                            visit(((ContainerArea) obj).droplist, visited, includeSource);
                        } else if (obj instanceof KeyArea) {
                            visit(((KeyArea) obj).dialogue, visited, includeSource);
                            if (((KeyArea) obj).requirement != null) {
                                visit(((KeyArea) obj).requirement.required_obj, visited, includeSource);
                            }
                        } else if (obj instanceof MapChange) {
                            visit(((MapChange) obj).map, visited, includeSource);
                        } else if (obj instanceof ReplaceArea) {
                            if (((ReplaceArea) obj).requirement != null) {
                                visit(((ReplaceArea) obj).requirement.required_obj, visited, includeSource);
                            }
                        } else if (obj instanceof RestArea) {
                            //Nothing to visit
                        } else if (obj instanceof ScriptArea) {
                            visit(((ScriptArea) obj).dialogue, visited, includeSource);
                        } else if (obj instanceof SignArea) {
                            visit(((SignArea) obj).dialogue, visited, includeSource);
                        } else if (obj instanceof SpawnArea) {
                            if (((SpawnArea) obj).spawnGroup != null) {
                                for (NPC npc : ((SpawnArea) obj).spawnGroup) {
                                    visit(npc, visited, includeSource);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (GameDataElement backlink : element.getBacklinks()) {
            visit(backlink, visited, includeSource);
        }
    }

    private static void visitSpritesheet(Spritesheet element, List<GameDataElement> visited, boolean includeSource) {
        //Nothing to visit

        //Not even the backlinks. Makes no sense.
    }


}
