@echo off
echo.
echo ***
echo *** createProject v1.0 (2024-08-24)
echo ***
echo *** This script will help to setup an ATCS project
echo *** and connect it to a repo on Github
echo ***
echo.
rem  Save this script anywhere on your PC  (e.g. C:\AT\createProject.bat)
rem  Then open a command shell:  CMD
rem  and call the script in the shell.

if "%1"=="" goto :noParamError

echo You have entered:
echo.
echo ATCS-project: %1  
echo in Directory: %2
echo Github user:  %3
echo.
pause
if "%3"=="" goto :help
if not "%4"=="" goto :help

if not exist %2 md %2

rem --- Create ATCS project (if not already done)
:atcs
if not exist %2\%1\created\drawable\char_hero.png (
    echo.
    echo Do the following steps:
    echo.
    echo 1.  Start ATCS
    echo 2.  Create your ATCS workspace in  %2
    echo 3.  Add the new project  %1
    echo 4.  Exit ATCS
    echo.
    pause
    goto :atcs
) else (
    echo ATCS Project exists
)

rem --- Create git repository
rem     This is the really important part :)
rem -- go into project dir (even if we do not need to create the git repo)
echo cd %2\%1
cd %2\%1

if not exist %2\%1\readme.md (
    echo creating readme

    echo # %1 > readme.md
) else (
    echo readme already exists
)

if not exist %2\%1\.gitignore (
    echo creating gitignore

    echo .workspace > .gitignore
    echo .project >> .gitignore
    echo .workspace.json >> .gitignore
    echo .project.json >> .gitignore
    echo altered/drawable >> .gitignore
    echo altered/drawable/* >> .gitignore
    echo created/drawable >> .gitignore
    echo created/drawable/* >> .gitignore
    echo tmp/res/values/loadresources.xml >> .gitignore
) else (
    echo gitignore already existed
)

:git
if not exist %2\%1\.git (
    echo Initializing git
    git init -b main || (
        echo "failed to init git. Please make sure it is installed"
        pause
        goto :git
    )
    :git_commit
    git add readme.md
    git add .gitignore
    git commit -m "Init project" || (
        echo "failed to create the initial commit."
        pause
        goto :git_commit
    )
    echo Done initializing git
) else (
    echo git was already initialized
)

echo.
echo Now create your repo %1 on https://github.com/%3  (if not already done)
echo.
pause
echo pushing to git repo https://github.com/%3/%1.git
git remote add origin https://github.com/%3/%1.git
git push -u origin main

echo.
echo At last you have to find the repo in your git client:
echo - In Smartgit: Menu option: Repository / Search for repository
echo.
goto :end

:noParamError
echo.
echo You have given no parameter. 
echo Maybe you have just double clicked it? That won't work.
echo I have opened a CMD shell for you. Enter the command in that shell.
start cmd
echo Please switch to the CMD shell

:help
echo.
echo Enter the command with 3 parameters:
echo %0  project  path  git-user
echo.
echo Example:  %0  feygard_1  c:\AT\ATCS  NutAndor
echo.

:end
echo.
echo *** End ***
pause
