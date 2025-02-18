!include MUI2.nsh

; Version will be passed as /DVERSION=vx.x.x
!define TRAINER_VERSION "0.1.5"
!define JAVA_BIN "java"
!define ATCS_SOURCE_DIR "..\..\"

Name "Andor's Trail Content Studio ${VERSION}"
OutFile "..\ATCS_${VERSION}_Setup.exe"
InstallDir "$PROGRAMFILES\ATCS\"

;SetCompressor /SOLID /FINAL lzma

Var StartMenuFolder

!define MUI_WELCOMEPAGE_TITLE "Welcome to Andor's Trail Content Studio installer"
!define MUI_WELCOMEPAGE_TEXT "This will install Andor's Trail Content Studio ${VERSION}"
!define MUI_FINISHPAGE_TEXT "Andor's Trail Content Studio ${VERSION} - Install completed !"
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "Andor's Trail Content Studio"
!define MUI_PAGE_HEADER_TEXT "Installing Andor's Trail Content Studio ${VERSION}"


;Start Menu Folder Page Configuration
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU" 
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\ATCS" 
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "ATCS"

!define MUI_HEADERIMAGE
;!define MUI_HEADER_TRANSPARENT_TEXT
!define MUI_HEADERIMAGE_BITMAP nsisHeader.bmp
!define MUI_HEADERIMAGE_BITMAP_NOSTRETCH
;!define MUI_HEADERIMAGE_RIGHT
;!define MUI_HEADERIMAGE_BITMAP_STRETCH "AspectFitHeight"
!define MUI_HEADERIMAGE_UNBITMAP nsisHeader.bmp
;!define MUI_HEADERIMAGE_UNBITMAP_STRETCH "AspectFitHeight"
!define MUI_WELCOMEFINISHPAGE_BITMAP nsisBorderBanner.bmp
!define MUI_UNWELCOMEFINISHPAGE_BITMAP nsisBorderBanner.bmp
;!define MUI_BGCOLOR "E3E3E3"
!define MUI_ABORTWARNING

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU "ATCS" $StartMenuFolder
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"


;------------------------------------------------------------------------------------
Section install

  ;--- Create in ...\packaging\common\   ATCS.cmd  ATCT.ico  ATCS.jar
  SetOutPath $INSTDIR
  file "ATCS.ico"
  file "${ATCS_SOURCE_DIR}\packaging\common\ATCS.jar"

  Call GetJRE
  Pop $R0
;  file "${ATCS_SOURCE_DIR}\packaging\common\ATCS.cmd"
;  !insertmacro _ReplaceInFile "ATCS.cmd" "java.exe" "$R0"  (It was too much work this way)
  FileOpen $9 "ATCS.cmd" w
  FileWrite $9 '@echo off$\r$\n'
  FileWrite $9 '$\r$\n'
  FileWrite $9 'set "ATCS_DIR=%~dp0"$\r$\n'
  FileWrite $9 'set "MAX_MEM=1024M"$\r$\n'
  FileWrite $9 'set "CP=%ATCS_DIR%lib\*"$\r$\n'
  FileWrite $9 'set "JAVA=$R0"$\r$\n'
  FileWrite $9 'set "JAVA_OPTS="$\r$\n'
  FileWrite $9 'set "ENV_FILE=%ATCS_DIR%ATCS.env.bat"$\r$\n'
  FileWrite $9 'set "MAIN_CLASS=com.gpl.rpg.atcontentstudio.ATContentStudio"$\r$\n'
  FileWrite $9 '$\r$\n'
  FileWrite $9 'if exist "%ENV_FILE%" ($\r$\n'
  FileWrite $9 '  call "%ENV_FILE%"$\r$\n'
  FileWrite $9 ') else ($\r$\n'
  FileWrite $9 '  echo REM set "MAX_MEM=%MAX_MEM%">"%ENV_FILE%"$\r$\n'
  FileWrite $9 '  echo REM set "JAVA=%JAVA%">>"%ENV_FILE%"$\r$\n'
  FileWrite $9 '  echo REM set "JAVA_OPTS=%JAVA_OPTS%">>"%ENV_FILE%"$\r$\n'
  FileWrite $9 '  echo.>>"%ENV_FILE%"$\r$\n'
  FileWrite $9 ')$\r$\n'
  FileWrite $9 '$\r$\n'
  FileWrite $9 'start "" "%JAVA%" %JAVA_OPTS% -Xmx%MAX_MEM% -jar ATCS.jar$\r$\n'
  FileClose $9

  WriteUninstaller "$INSTDIR\Uninstall.exe"


  !insertmacro MUI_STARTMENU_WRITE_BEGIN "ATCS"

    ;--- Create shortcuts
    CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
    CreateShortcut "$SMPROGRAMS\$StartMenuFolder\Andor's Trail Content Studio.lnk" "$INSTDIR\ATCS.cmd" "" "$INSTDIR\ATCS.ico"
    CreateShortcut "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk" "$INSTDIR\Uninstall.exe"

  !insertmacro MUI_STARTMENU_WRITE_END

SectionEnd


;------------------------------------------------------------------------------------
Section uninstall

  Delete "$INSTDIR\lib\jide-oss.jar"
  Delete "$INSTDIR\lib\ui.jar"
  Delete "$INSTDIR\lib\junit-4.10.jar"
  Delete "$INSTDIR\lib\json_simple-1.1.jar"
  Delete "$INSTDIR\lib\AndorsTrainer_v${TRAINER_VERSION}.jar"
  Delete "$INSTDIR\lib\rsyntaxtextarea.jar"
  Delete "$INSTDIR\lib\prefuse.jar"
  Delete "$INSTDIR\lib\bsh-2.0b4.jar"
  Delete "$INSTDIR\lib\jsoup-1.10.2.jar"
  RMDir "$INSTDIR\lib\"

  Delete "$INSTDIR\ATCS.ico"
  Delete "$INSTDIR\ATCS.cmd"
  Delete "$INSTDIR\ATCS.env.bat"
  Delete "$INSTDIR\ATCS.jar"
  Delete "$INSTDIR\Uninstall.exe"
  RMDir "$INSTDIR"

  !insertmacro MUI_STARTMENU_GETFOLDER "ATCS" $StartMenuFolder

  Delete "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk"
  Delete "$SMPROGRAMS\$StartMenuFolder\Andor's Trail Content Studio.lnk"
  RMDir "$SMPROGRAMS\$StartMenuFolder"

SectionEnd


;------------------------------------------------------------------------------------
Function GetJRE
;
;  Find JRE (java.exe)
;  DISABLED 1 - in .\jre directory (JRE Installed with application)
;  2 - in JAVA_HOME environment variable
;  3 - in the registry
;  4 - assume java.exe in current dir or PATH

  Push $R0
  Push $R1

  ;ClearErrors
  ;StrCpy $R0 "$EXEDIR\jre\bin\java.exe"
  ;IfFileExists $R0 JreFound
  ;StrCpy $R0 ""

  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\${JAVA_BIN}.exe"
  IfErrors 0 JreFound

  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\${JAVA_BIN}.exe"

  IfErrors 0 JreFound
  StrCpy $R0 "${JAVA_BIN}.exe"

 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd
