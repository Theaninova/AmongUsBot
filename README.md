# Among Us Bot

Provides a very intuitive interface to keep track of colors and dead people.
As a bonus it can deafen lazy people. Caveat: Discord rate limits bots, it can roughly mute
two people per second. Use this as a last resort, not as a default.

The bot reacts automatically when someone deafens themselves, waits 5 seconds and deafens
anyone who is not deafened yet (except dead/spectator people).

To update the game code, just type a new game code in chat, the message will update automatically.

Invite him [here](https://discord.com/api/oauth2/authorize?client_id=759194955272552468&permissions=29715520&scope=bot)

## Commands

| **Command**      | **Arguments**                                          | **Info**                                                                                          |
|------------------|--------------------------------------------------------|--------------------|
| !asnew            |    [Game Code]  | Create a new game (has to be in Voice channel)  |
| !unmute         |        | Unmute/Undeafen yourself if the bot didn't do that automatically   |
| !undeafen        |     | Unmute/Undeafen yourself if the bot didn't do that automatically    |
| !help           |    | Show a help dialog with all these commands                                                        |
| <code>([A-z]&#124;\d){5}</code> |     |  Create a new game (has to be in Voice channel). Does not mute anyone.  |

## Preview

![Imgur](https://imgur.com/7Hg1qeB.png)

To mute all just deafen yourself or click the mute emote.
Dead people will not be deafened, but are muted during meetings.

## Self Hosting
I'm not quite sure how good the bot scales, so there is a chance that I will
disable it on other servers in the future.

You can still self-host it then.

Start the server with `java -jar [server-file-name]` If you put in everything correctly,
the bot should message you on Discord.

*Note:* You need to invite the bot into a server before it can message you.

Run it once (it should crash or print an error), so `config.json` is being created.
Add your Discord ID `adminId` (not name), Bot token `token` to the `config.json`.

If you verified that everything works correctly, you can start the server in the background, on Linux that is
`nohup java -jar [server-file-name]`. To stop it you can either type `!stop` in the Admin Console (Discord PM) or
if the bot is unresponsive the PID of it through `ps -ef` and `kill [pid]`.

Last this is adding custom emotes. This is semi-optional, there are fallback default emotes,
however they do not cover the full spectrum of colors used by Among Us.

The bot will look for emojis named `amongus_[color]`, I recommend using [these](https://among-us.fandom.com/wiki/Category:Colors).

<p align="left">
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/a/a6/1_red.png/revision/latest/scale-to-width-down/140?cb=20200912125145" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/8/8e/2_blue.png/revision/latest/scale-to-width-down/140?cb=20200912125155" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/3/34/3_green.png/revision/latest/scale-to-width-down/140?cb=20200912125201" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/9/9b/4_pink.png/revision/latest/scale-to-width-down/140?cb=20200912125206" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/f/f1/5_orange.png/revision/latest/scale-to-width-down/140?cb=20200912125212" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/5/54/6_yellow.png/revision/latest/scale-to-width-down/140?cb=20200912125217" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/5/55/7_black.png/revision/latest/scale-to-width-down/140?cb=20200912125223" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/e/e1/8_white.png/revision/latest/scale-to-width-down/140?cb=20200912125229" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/7/72/9_purple.png/revision/latest/scale-to-width-down/140?cb=20200912125234" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/b/b2/10_brown.png/revision/latest/scale-to-width-down/140?cb=20200912125240" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/f/f2/11_cyan.png/revision/latest/scale-to-width-down/140?cb=20200912125246" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/f/fd/12_lime.png/revision/latest/scale-to-width-down/140?cb=20200912125258" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/8/87/Tan.png/revision/latest?cb=20200921181340" width="32"/>
    <img src="https://vignette.wikia.nocookie.net/among-us-wiki/images/1/1a/Player_fortegreen.png/revision/latest?cb=20200917134119" width="32"/>
</p>

### Compiling yourself
I developed it under Windows, and had some trouble compiling it on Linux. You mileage may vary.

### Admin commands

| **Command**      | **Arguments**                                          | **Info**                                                                                          |
|------------------|--------------------------------------------------------|--------------------|
| !info            |     | Info about the bot's status  |
| !servers         |        | List of servers the bot joined   |
| !stop        |     | Stop the bot |
| !help           |    | Show a help dialog with all these commands                                                        |

### Config File

```json5
{
  // your user ID
  botAdmin: 12345678,
  // your discord bot token
  token: "YourTokenHere",
  // delay before the bot will deafen all
  muteDelay: 5000
}
```
