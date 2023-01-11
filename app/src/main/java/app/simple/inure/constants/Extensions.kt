package app.simple.inure.constants

object Extensions {

    /**
     * List of all the image extensions
     */
    val imageExtensions = hashMapOf(
            "jpg" to true,
            "jpeg" to true,
            "png" to true,
            "gif" to true,
            "webp" to true,
            "bmp" to true,
            "wbmp" to true,
            "ico" to true,
            "svg" to true,
            "svgz" to true,
            "tiff" to true,
            "tif" to true,
            "jpe" to true,
            "jfif" to true,
            "dib" to true,
            "heif" to true,
            "heic" to true,
            "cur" to true,
            "fif" to true,
            "psd" to true,
            "pspimage" to true,
            "xcf" to true,
            "apng" to true,
            "avif" to true,
            "jng" to true,
            "mng" to true,
            "pcx" to true,
            "pic" to true,
            "raw" to true,
            "tga" to true,
            "dds" to true,
            "exr" to true,
            "hdr" to true,
            "jxr" to true,
            "pam" to true,
            "pgf" to true,
            "sgi" to true,
            "rgb" to true,
            "rgba" to true,
            "bw" to true,
            "int" to true,
            "inta" to true,
            "sid" to true,
            "ras" to true,
            "sun" to true,
            "tga" to true,
            "xbm" to true,
            "xpm" to true,
            "3dv" to true,
            "amf" to true,
            "ai" to true,
            "awg" to true,
            "cgm" to true,
            "cdr" to true,
            "cmx" to true,
            "dxf" to true,
            "e2d" to true,
            "egt" to true,
            "eps" to true,
            "fs" to true,
            "gbr" to true,
            "odg" to true,
            "svg" to true,
            "stl" to true,
            "vrml" to true,
            "x3d" to true,
    )

    /**
     * random color for all image file extensions
     */
    val imageExtensionColors = hashMapOf(
            "jpg" to "#F44336",
            "jpeg" to "#E91E63",
            "png" to "#9C27B0",
            "gif" to "#673AB7",
            "webp" to "#3F51B5",
            "bmp" to "#2196F3",
            "wbmp" to "#03A9F4",
            "ico" to "#00BCD4",
            "svg" to "#009688",
            "svgz" to "#4CAF50",
            "tiff" to "#8BC34A",
            "tif" to "#CDDC39",
            "jpe" to "#FFEB3B",
            "jfif" to "#FFC107",
            "dib" to "#FF9800",
            "heif" to "#FF5722",
            "heic" to "#795548",
            "cur" to "#9E9E9E",
            "fif" to "#607D8B",
            "psd" to "#FF5252",
            "pspimage" to "#FF4081",
            "xcf" to "#E040FB",
            "apng" to "#7C4DFF",
            "avif" to "#536DFE",
            "jng" to "#448AFF",
            "mng" to "#40C4FF",
            "pcx" to "#18FFFF",
            "pic" to "#64FFDA",
            "raw" to "#69F0AE",
            "tga" to "#B2FF59",
            "dds" to "#EEFF41",
            "exr" to "#FFFF00",
            "hdr" to "#FFD740",
            "jxr" to "#FFAB40",
            "pam" to "#FF6E40",
            "pgf" to "#FF3D00",
            "sgi" to "#DD2C00",
            "rgb" to "#FF1744",
            "rgba" to "#D50000",
            "bw" to "#FF80AB",
            "int" to "#FF4081",
            "inta" to "#F50057",
            "sid" to "#C51162",
            "ras" to "#AA00FF",
            "sun" to "#6200EA",
            "tga" to "#304FFE",
            "xbm" to "#2962FF",
            "xpm" to "#0091EA",
            "3dv" to "#00B8D4",
            "amf" to "#00BFA5",
            "ai" to "#00C853",
            "awg" to "#64DD17",
            "cgm" to "#AEEA00",
            "cdr" to "#FFD600",
            "cmx" to "#FFAB00",
            "dxf" to "#FF6D00",
            "e2d" to "#DD2C00",
            "egt" to "#FF3D00",
            "eps" to "#FF6E40",
            "fs" to "#FFAB40",
            "gbr" to "#FFD740",
            "odg" to "#FFFF00",
            "stl" to "#FF6E40",
            "vrml" to "#FF3D00",
    )

    val nonImageFileExtensionColors = hashMapOf(
            "txt" to "#FF5252",
            "md" to "#FF4081",
            "html" to "#E040FB",
            "htm" to "#7C4DFF",
            "xml" to "#536DFE",
            "json" to "#448AFF",
            "js" to "#40C4FF",
            "css" to "#18FFFF",
            "scss" to "#64FFDA",
            "sass" to "#69F0AE",
            "less" to "#B2FF59",
            "py" to "#EEFF41",
            "java" to "#FFFF00",
            "kt" to "#FFD740",
            "cpp" to "#FFAB40",
            "c" to "#FF6E40",
            "h" to "#FF3D00",
            "hpp" to "#DD2C00",
            "cs" to "#FF1744",
            "php" to "#D50000",
            "rb" to "#FF80AB",
            "go" to "#FF4081",
            "swift" to "#F50057",
            "rs" to "#C51162",
            "dart" to "#AA00FF",
            "sh" to "#6200EA",
            "bat" to "#304FFE",
            "ps1" to "#2962FF",
            "psm1" to "#0091EA",
            "psd1" to "#00B8D4",
            "ps1xml" to "#00BFA5",
            "psm1xml" to "#00C853",
            "psd1xml" to "#64DD17",
            "ps1json" to "#AEEA00",
            "psm1json" to "#FFD600",
            "psd1json" to "#FFAB00",
            "ps1yaml" to "#FF6D00",
            "psm1yaml" to "#DD2C00",
            "psd1yaml" to "#FF3D00",
            "ps1yml" to "#FF6E40",
            "psm1yml" to "#FFAB40",
            "psd1yml" to "#FFD740",
            "ps1txt" to "#FFFF00",
            "psm1txt" to "#FF6E40",
            "ini" to "#FF3D00",
            "gz" to "#FF5252",
            "zip" to "#FF4081",
            "kotlin_builtins" to "#E040FB",
            "kotlinx.coroutines" to "#7C4DFF",
            "bin" to "#536DFE",
            "dat" to "#448AFF",
            "properties" to "#40C4FF",
            "yml" to "#18FFFF",
            "yaml" to "#64FFDA",
            "toml" to "#69F0AE",
            "sql" to "#B2FF59",
            "key" to "#EEFF41",
            "pem" to "#FFFF00",
            "crt" to "#FFD740",
            "cer" to "#FFAB40",
            "der" to "#FF6E40",
            "pfx" to "#FF3D00",
            "p12" to "#DD2C00",
            "p7b" to "#FF1744",
            "p7c" to "#D50000",
            "p7r" to "#FF80AB",
            "p7s" to "#FF4081",
            "p7m" to "#F50057",
            "p7x" to "#C51162",
            "p8" to "#AA00FF",
            "prof" to "#6200EA",
            "profm" to "#304FFE",
            "sln" to "#2962FF",
            "suo" to "#0091EA",
            "rsa" to "#00B8D4",
            "sf" to "#00BFA5",
            "0" to "#00C853",
            "1" to "#64DD17",
            "version" to "#AEEA00",
            "dll" to "#FFD600",
            "exe" to "#FFAB00",
            "so" to "#FF6D00",
            "dylib" to "#DD2C00",
            "lib" to "#FF3D00",
            "a" to "#FF6E40",
            "o" to "#FFAB40",
            "obj" to "#FFD740",
            "class" to "#FFFF00",
            "jar" to "#FF6E40",
            "war" to "#FF3D00",
            "ear" to "#FF5252",
            "apk" to "#FF4081",
            "ipa" to "#E040FB",
            "so" to "#448AFF",
            "ttf" to "#40C4FF",
            "otf" to "#18FFFF",
    )
}