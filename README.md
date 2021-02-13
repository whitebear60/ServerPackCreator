# ServerPackCreator (WIP Title)
Java programm to create a server pack from a given 1.16.5 Minecraft modpack (only SCP4 at first)

# WIP! CURRENTLY NOT FOR PUBLIC CONSUMPTION
Use at your own risk! You shouldn't use it atm.
Seriously bro, don't.

Planned:
Take any given modpack, if possible for any given Forge version, and:
- Copy all config related files to a "server_pack" directory
    - "config", "defaultconfigs", "scripts" etc. etc.
- Copy all mod related files to a "server_pack" directory
    - Delete client side mods (make this configurable so users can customize the list of mods to be deleted by ServerPackCreator)
- Allow specifiying custom directories to include in server_pack
- Download and install Forge server (make this configurable so users can specify their forge version)
- Copy server-icon.png to a "server_pack" directory and make the source configurable
    - Make creation of server-icon.png optional
    - Provide default
- Create a server.properties file in "server_pack" directory and make this configurable so users can set their defaults once and SPC will take care of the rest
    - Make creation of server.properties optional
- Create default start scripts for Linux and Windows systems based on forge version installed
    - Make creation of start scripts optional
- Automatically create zip-archive of the created serverpack with a configurable filename alongside unpacked serverpack
    - Make creation of archive optional
    
Supporting Fabric or any other modloader is not planned. Feel free to fork and do whatever you want with this software. It's MIT License for a reason.