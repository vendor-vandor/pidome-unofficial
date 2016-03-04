# README #

This repository is for a test setup of a PiDome connected photoframe. It is in it's early stages but already usable. It runs on Java and uses the available hardware acceleration available on the raspberry Pi. Although it 
is being developped to be used with the PiDome server you are not required to have it running as the photo frame software also has a stand alone option.

Here you can see it in action on a barebone diisplat attached to a Raspberry Pi: https://www.youtube.com/watch?v=x97XjtNdvmM

This app has some limitations, pictures can not be larger then 2000x2000 pixels and must be png and non progressive jpg files. Also because
this application uses the Raspberry Pi's framebuffer it is very wise to do a 50/50 memory split on a Raspberry Pi model B (256MB). Overclocking is not needed. The app runs fine on different kind
of resolutions and different aspect ratios. The app will try to fill the screen with respect to your pictures ratios and sizes.

If you encounter the app not being full screen, or you have black bars etc, check your framebuffer settings (/boot/config.txt). 
Information on this on: https://pidome.wordpress.com/2013/08/22/raspberry-pi-monitordisplay-resolution-problems/, the raspberry pi forum: http://www.raspberrypi.org/forums/viewtopic.php?t=5851 
or on google: https://www.google.nl/?q=raspberry+pi+framebuffer+settings

Current features:

* Show current weather or not (from the server's weather plugin),
* Show current time or not (Time received from the server),
* Photo rotations with fading or all kind of different other rotation types
* Remove/add photos any time.

Photos are shown in random order but always once per iteration. This means the app reads the photos list, runs it, and recreates the list after all photo's has been shown. This is done by indexing the photos directory.

## Setup ##
Follow the below to get up and running. The easiest is that when you configure the client for the first time you have a mouse and keyboard attached to the raspberry pi where this client is installed on.

Important:
I refer to .default.properties files, these files are default properties which are minimal needed to run the app. Only change these the very first time before you start the application. 
When the app is succesfully started it creates files without the text "default" in them like "application.properties" When you see these files edit them instead of the default ones.

### FIRST ###
This little app makes use of an X11 server to project to the screen (future versions may be able to do this an other way). This means that the X11 Server on the Pi must be started before the app can run. But, the startup script will take care of this. With the default Raspberry Pi installation X11 is installed so you only have to install one thing which is called xterm. Do this by issuing the command:
"sudo apt-get install xterm"

After installing you only have to do one thing to get you going which is change the permissions on the file "run.sh". Change these with "chmod 755 run.sh".

###How to start the app###
Because X11 is used you will be needing the X11 server. You do not need a window manager. To accomplish this instruct X11 to start and only run a single application. You can do this by:
"startx ./run.sh"
This command can be executed on both the main console, or by using a terminal application like putty, login remotely and start. When you start the app from a remote terminal you probably want to detach it
from the terminal. If you issue the command like this: "startx ./run.sh > log.txt 2>&1 &" all output will be put into the file log.txt and the process is detached so you can log out.

## Configuration ##
Follow the below to configure the app. This only has to be done once!

### Without PiDome server ###
If you are not using the PiDome server open the file "settings/preferences.default.properties" change the line standalone=false to standalone=true. Running in stand alone disables the time and weather and all other future planned server interactions.

### With PiDome server ###
There are multiple ways to configure the app when using with the server. First you need to create a client on the server:
Log in on the server and go to "Clients / Users / Persons"> "Fixed clients". Add an username and password.

#### Server has auto detect enabled ####
By the default the server has broadcasting enabled which massively can easy up configurations of clients. If you have a mouse and keyboard attached to the Raspberry Pi you can now start the application. The app will start search for the server and will ask you for your clients credentials. If you do not have these attached open the file "settings/application.defaults.properties" and enter the username and password here.

#### Server has auto detect disabled ####
When broadcasting is disabled on the server you have to enter all the settings manually. If you have a mouse and keyboard attached to the Raspberry Pi you can now start the application. The app will start search for the server and will ask you for the server's location settings. If you do not have these attached open the file "settings/application.defaults.properties" and enter the server information here. You will need to enter the following information:

First, open the server and look at the "Server status" page and scroll down to availability

When SSL is enabled on the server (default):

    server.socket.port.ssl=enter the client service TLS/SSL port number here
    server.socket.port=0
    server.address=enter the ip address of the server here
    server.http.port=enter the SSL port of the http services here.
    server.http.port.ssl=true
    broadcast.port=10000

When SSL is disabled on the server:

    server.socket.port.ssl=0
    server.socket.port=Enter the client service default port here (Not the TLS/SSL port)
    server.address=enter the ip address of the server here
    server.http.port=enter the http default port here
    server.http.port.ssl=false
    broadcast.port=10000

You can now start the app.

## Use the app ##
There ain't much you can do yet, but, you can put photo's as this app is meant to show them. You will be able to add and remove photos while the app is running and there is no need to stop.
There is a folder called photos where you can put them. There currently are some limitations though! Photo dimensions must be below 2000X2000 pixels and they can only be png or non progressive jpg files.

When a keyboard is attached you can stop the app by pressing escape any time.

## Preferences ##
The app has a couple of options you currently can set. You can find them in preferences.default.properties

## Known issues ##
When you stop the app by killing it from the process list it is possible it leaves the connection to the server open. On the server go to "Clients / Users / Persons"> "Fixed clients" click the client and press the disconnect button.
If you have killed the app and restarted it because you have changed some preferences, and you are getting the message client already logged in, follow the above steps and re-enter the credentials on the client (if keyboard attached) or kill it and restart.

The run.sh file needs to point to the installation of your JDK. Only the Oracle JDK has been tested.