# BTEMapV2 - Polymap Minecraft Plugin

This plugin connects the BTE Server to the BTE Map.

## Features

- Send player locations to the socket.io running on the map.
- Link the players minecraft account to the map application.
- Create regions to be then displayed on the map (NFW).

## Planned Updates

- Verify/Fix the linking command.
- Automatic linking of minecraft accounts to the map using DiscordSRV.
- Fix the region creation command.

## Instillation

1. Set up the BTE polymap application: <https://github.com/BTE-Canada-USA/polymap>
2. Copy the latest release jar file to your `plugins` folder on the minecraft server.
3. Restart the server.
4. Update the configuration files located in `plugins/BTEMapV2/`
5. Restart the server again.
6. If you see any errors from this plugin, you may need to go back to step 4.
7. Play On!

## Compile Instructions

1. Make sure you have Maven and Java 1.8 Installed.
2. Clone the repository.
3. Run the maven command `mvn install`
4. Run the maven command `mvn package`
5. Copy the `btemap-xx.jar` file to the plugins file on your server.
6. Restart the server.
