# GPSAlarm

The GPSAlarm is a simple tool made in Android Studio 2.2.2. for waking up the user once it comes in radius around the destination. 

Main features include:
* Customisable alarm;
* Setting your destination using the touchscreen OR searchbar;
* Setting up a list of favorite destinations.

GPS Alarm currently supports all Android versions for 4 and up.

Application requires an active GPS uplink, and having a network connection is a plus.

# Installation

Clone the repository and compile it for your device using Android Studio or use this download link for the .APK file: https://www.dropbox.com/s/yeqxvs61k1xf1gf/GPSAlarm.apk?dl=0

# Instructions

* Set your destination by either searching for the adress in the searchbar at the top of the screen or by performing a long press on your desired location. The application is now tracking your progress towards the marker, and will issue an alarm once you reach your destination.

* To add your currently placed marker to the list of My Alarms, you need to tap on the marker and then perform a long press on the popup that points towards the marker. You will be prompted to choose a name for your saved marker. Access your marker anytime using the menu at the top right side of the screen from the menu item My Alarms.

* The top right of the application contains a menu with several options. 
  * My Alarms contains the list of markers you have saved. Tap on your desired location to immediately place a marker on it and start the tracking process. You can remove items from this list by performing a long press on your desired item.
  * The Settings menu contains several settings for the customization of your alarm. Options include:
    * Custom alarm sound from your phone's library;
    * Ability to set the radius(distance) when you will be notified;
    * Selecting a map type(normal, satellite, hybrid);
    * Selecting the frequency at which the program updates your location and checks if the destination is reached.
  * Help menu contains largely the same information as this paragraph.
  
# Planned features

A cleaner UI - at the current iteration, the UI still needs more unification in terms of design.

A notification in the taskbar for clarity.


