var mineflayer = require('mineflayer');
var v = require('vec3');

var bot = mineflayer.createBot({
    host: "localhost",
    username: (process.argv[2] ? process.argv[2] : "Bot")
});

// install the plugin
require('mineflayer-navigate')(mineflayer)(bot);

bot.navigate.on('pathFound', function (path) {
    bot.chat("[BOT] I found a path! I can get there in " + path.length + " moves.");
});
bot.navigate.on('cannotFind', function (closestPath) {
    bot.chat("[BOT] Unable to find a path, getting as close as possible...");
    bot.navigate.walk(closestPath);
});
bot.navigate.on('arrived', function () {
    bot.chat("[BOT] I'm there!");
});
bot.navigate.on('interrupted', function() {
    bot.chat("[BOT] Stopped.");
});
bot.on('whisper', function(username, message) {
    // navigate to whoever talks
    if (username === bot.username) return;

    var target = bot.players[username].entity; if (target == null) return bot.chat("[BOT] " + username + ", I don't know where you are!");
    var funct = function() {};

    if (![
        "mo_mar"
    ].indexOf(username) == -1) return bot.chat("[BOT] Sorry " + username + ", you aren't in my list of trusted users.");

    if (message.toLowerCase().match(/^(?:come|go)\s*to(?: |$)/)) {
        var coord = message.toLowerCase().match(/^(?:come|go)\s*to\s*(~?-?\d+)\s*(~?-?\d+)\s*(~?-?\d+)/);
        if (!coord) return bot.chat("[BOT] " + username + ", please use the format \"/tell " + bot.username + " come to [~]<X> [~]<Y> [~]<Z>\"");
        console.log(coord);
        var x = coord[1]; if (x.indexOf("~") == 0) x = parseInt(x.toString().substring(1)) + target.position.x;
        var y = coord[2]; if (y.indexOf("~") == 0) y = parseInt(y.toString().substring(1)) + target.position.y;
        var z = coord[3]; if (z.indexOf("~") == 0) z = parseInt(z.toString().substring(1)) + target.position.z;
        console.log(x,y,z);

        bot.chat("[BOT] Okay " + username + ", I'll go there, but it could take a few seconds to calculate the path.");
        bot.navigate.to(v(x, y, z));
    }
    else if (message.toLowerCase().match(/^come(?:\s|$)/)) {
        bot.chat("[BOT] Okay " + username + ", I'll come to you, but it could take a few seconds to calculate the path.");
        bot.navigate.to(target.position);
    }
    else if (message.toLowerCase().match(/^stop(?:\s|$)/)) bot.navigate.stop();
    else bot.chat("[BOT] Sorry " + username + ", I couln't understand what I shall do.");
});
