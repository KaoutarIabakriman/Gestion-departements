@echo off
chcp 65001>nul
setlocal enabledelayedexpansion

:: ========== CONFIGURATION ==========
set "PROJECT_ROOT=%cd%"
set "SOURCE_DIR=src\main\java\com\test\gestiondepartements"
set "TARGET_DIRS=Config Controller Dto Entities Security Service"
set "IGNORE_DIRS=\\.git\\ \\.idea\\ \\out\\ \\target\\"

echo [INFO] Copie des fichiers Java dans le presse-papiers...
echo [INFO] Racine du projet : %PROJECT_ROOT%
echo.

:: ========== FICHIER TEMPORAIRE ==========
set "TEMP_FILE=%temp%\gestion_depts_java_%random%.txt"
echo ===== GESTIONDEPARTEMENTS - FICHIERS JAVA ===== > "%TEMP_FILE%"
echo. >> "%TEMP_FILE%"

:: ========== PARCOURS ET EXTRACTION ==========
for %%d in (%TARGET_DIRS%) do (
    if exist "%SOURCE_DIR%\%%d" (
        echo ----- DOSSIER: %%d ----- >> "%TEMP_FILE%"
        echo. >> "%TEMP_FILE%"
        dir /s /b /a-d "%SOURCE_DIR%\%%d\*.java" 2>nul | findstr /v /i "%IGNORE_DIRS%" > "%temp%\filelist.tmp"
        for /f "usebackq delims=" %%f in ("%temp%\filelist.tmp") do (
            echo ===== FICHIER: %%~nxf ===== >> "%TEMP_FILE%"
            echo [Chemin: %%f] >> "%TEMP_FILE%"
            echo. >> "%TEMP_FILE%"
            type "%%f" >> "%TEMP_FILE%"
            echo. >> "%TEMP_FILE%"
        )
        del "%temp%\filelist.tmp" 2>nul
    )
)

:: ========== COPIE DANS LE PRESSE-PAPIERS ==========
powershell -command "Get-Content '%TEMP_FILE%' | clip"

:: ========== NETTOYAGE ET MESSAGE ==========
del "%TEMP_FILE%" 2>nul
echo [SUCCES] Contenu Java copié dans le presse-papiers
echo Vous pouvez maintenant coller (Ctrl+V) le contenu où vous voulez.
endlocal
