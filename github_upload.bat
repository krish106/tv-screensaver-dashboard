@echo off
set /p repo_url="Enter your new GitHub Repository URL: "

echo Initializing Git...
git init

echo Checking Git configuration...
set GIT_EMAIL=
set GIT_NAME=
for /f "tokens=*" %%a in ('git config user.email') do set GIT_EMAIL=%%a
for /f "tokens=*" %%b in ('git config user.name') do set GIT_NAME=%%b

if "%GIT_EMAIL%"=="" goto ConfigureGit
if "%GIT_NAME%"=="" goto ConfigureGit
goto AddFiles

:ConfigureGit
echo.
echo Git identity is not configured correctly.
set /p git_email="Enter your email for Git: "
set /p git_name="Enter your name for Git: "
git config user.email "%git_email%"
git config user.name "%git_name%"
echo Configured User: %git_name% (%git_email%)

:AddFiles
echo Adding files...
git add .

echo Committing...
git commit -m "Initial commit: Project cleanup and release preparation"

echo Adding remote...
git remote remove origin 2>nul
git remote add origin %repo_url%

echo Pushing to GitHub...
git branch -M main
git push -u origin main --force

echo.
echo Done! Go to your repository and create a Release with the binaries in the 'releases' folder.
pause
