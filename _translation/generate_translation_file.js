var fs = require("fs");
var recursive = require("recursive-readdir");

var regex = /\$\.t\((?:"((?:[^"]|\\")+)"|'((?:[^']|\\')+)')\s*,\s*(?:"((?:[^"]|\\")+)"|'((?:[^']|\\')+)')/g;
var translations = "";

process.chdir(__dirname);
recursive("../", function (err, files) {
    for (var i in files) { var file = files[i];
        if (file.toLowerCase().indexOf(".java") == file.length - 5) console.log("Processing: " + file.substr(2));
        var content = fs.readFileSync(file).toString();
        var match;
        while (match = regex.exec(content)) {
            var tid = (match[1] ? match[1] : match[2]);
            var def = (match[3] ? match[3] : match[4]);
            if (!translations.match('/^' + tid.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&") + ': /g'))
                translations += tid + ": '" + def + "'\n";
        }
    }
    console.log((translations.split("\n").length - 1) + " translations found.");
    fs.writeFileSync("../src/main/resources/translation.yml", translations);
    console.log("Wrote translations file to /src/main/resources/translation.yml");
    console.log("--------------------------------------------------------------------------------");
    console.log(translations);
});
