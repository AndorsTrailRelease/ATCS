#!/bin/bash

# Script to build ATCS.jar, replicating IntelliJ artifact definition
# Linux and Windows compatible

# --- Platform Detection ---
if [ "$1" = "-windows" ]; then
    echo "Got '-windows' flag. Running Windows version"
    PLATFORM="WINDOWS"
else
    echo "No '-windows' flag. Running Linux version"
    PLATFORM="LINUX"
fi

# --- Configuration ---
PACKAGING_DIR=$(dirname "$(readlink -f "$0" || greadlink -f "$0" || stat -f "$0")")
ATCS_SOURCE_DIR=$(dirname "${PACKAGING_DIR}")
TEMP_DIR="${PACKAGING_DIR}/tmp"
JAR_LOCATION="${PACKAGING_DIR}/ATCS.jar" # Output JAR location as per script
MANIFEST_LOCATION="${PACKAGING_DIR}/Manifest.txt"
VERSION_FILE="${ATCS_SOURCE_DIR}/res/ATCS_latest"
SOURCE_BASE_DIR="${ATCS_SOURCE_DIR}/src" # Base directory for standard source code
LIB_BASE_DIR="${ATCS_SOURCE_DIR}/lib"     # Base directory for libraries

# --- **ADDITIONAL SOURCE CODE FOLDERS** ---
EXTRA_SOURCE_DIRS=(
  "hacked-libtiled"
  "minify"
  "siphash-zackehh/src/main/java"
)

# --- Libraries to include ---
LIBRARIES=(
  "bsh-2.0b4.jar"
  "jide-oss.jar"
  "json_simple-1.1.jar"
  "jsoup-1.10.2.jar"
  "junit-4.10.jar"
  "prefuse.jar"
  "rsyntaxtextarea.jar"
  "ui.jar"
)

# --- Get version ---
echo "Getting version"
VERSION=$(tr -d '[:space:]' < "${VERSION_FILE}")
echo "Got version ${VERSION}"

# --- Prepare temporary directory ---
echo "Removing tmp folder"
rm -rf "${TEMP_DIR}"
echo "Recreating tmp folder"
mkdir -p "${TEMP_DIR}"

# --- **EXTRACT lib files directly to TEMP_DIR** ---
echo 'Extracting lib files to TEMP_DIR'
for LIB in "${LIBRARIES[@]}"; do
    echo "Extracting library: ${LIB}"
    unzip -qo "${LIB_BASE_DIR}/${LIB}" -d "${TEMP_DIR}" # Extract JAR contents to TEMP_DIR root
done

# --- Set ClassPath ---
echo "Getting source files"
# Find all java files in source directories
SOURCE_FILES=$(find "${SOURCE_BASE_DIR}" "${EXTRA_SOURCE_DIRS[@]/#/${ATCS_SOURCE_DIR}/}" -name "*.java" -print)
#echo "SourceFiles: ${SOURCE_FILES}"
echo ""

# --- Build Java classes ---
echo 'Building java classes'

# shellcheck disable=SC2086
# (we need word splitting here to pass multiple files)
javac -cp "${TEMP_DIR}" -d "${TEMP_DIR}" ${SOURCE_FILES}
if [ $? -ne 0 ]; then
    echo "Compilation failed. Please check errors above."
    exit 1
fi
echo "Compilation successful"

# --- Copy res stuff to temp folder ---
echo "Copying some stuff to temp folder"
cp -r "${ATCS_SOURCE_DIR}"/res/* "${TEMP_DIR}/"
mkdir -p "${TEMP_DIR}/com/gpl/rpg/atcontentstudio/img"
mkdir -p "${TEMP_DIR}/tiled/io/resources/"
cp -r "${ATCS_SOURCE_DIR}"/src/com/gpl/rpg/atcontentstudio/img/* "${TEMP_DIR}/com/gpl/rpg/atcontentstudio/img/" # some icons
cp -r "${ATCS_SOURCE_DIR}"/hacked-libtiled/tiled/io/resources/* "${TEMP_DIR}/tiled/io/resources/" # dtd file for tmx maps
cp "${VERSION_FILE}" "${TEMP_DIR}/" # Copy version file

# --- Create JAR file ---
echo ""
echo "Creating jar at location: ${JAR_LOCATION}"

cd "${TEMP_DIR}" || exit # Change to temp dir for JAR command

# JAR command WITHOUT lib directory
jar cfm "${JAR_LOCATION}" "${MANIFEST_LOCATION}"  -C . .
if [ $? -ne 0 ]; then
    echo "JAR creation failed."
    exit 1
fi

cd "${PACKAGING_DIR}" || exit # Go back to packaging dir

echo ''
echo "Done creating jar at ${JAR_LOCATION}"
cp -f "${JAR_LOCATION}" "${PACKAGING_DIR}/common/ATCS.jar" # Copy JAR to versioned name

# --- Create archive ---
cd "${PACKAGING_DIR}" || exit
echo "Creating archive"
if [ "$PLATFORM" = "WINDOWS" ]; then
    # Use PowerShell's Compress-Archive which is available by default on Windows
    powershell.exe -Command "Compress-Archive -Path './common/*' -DestinationPath './ATCS_${VERSION}.zip' -Force"
else
    # Use zip command on Linux
    zip -r "ATCS_${VERSION}.zip" common/* # archive the 'common' folder which now contains the JAR and libs
fi
if [ $? -ne 0 ]; then
    echo "Archive creation failed."
    exit 1
fi
echo "Created archive at ${PACKAGING_DIR}/ATCS_${VERSION}.zip"

echo "Script finished."
