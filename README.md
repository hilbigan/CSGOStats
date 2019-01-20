# CSGOStats

[Reddit Thread](https://www.reddit.com/r/GlobalOffensive/comments/ahz73s/wrote_a_small_script_to_analyze_my_1475/)

# Download

[Download-Link](http://flybig.eu/CSGOStats.jar)

## Usage:
- Install the "Ban Checker" Browser Plugin (Chrome/Firefox)
- Navigate to **(Your steam profile) -> Games -> (On CS:GO, click on personal data) -> Competitive Games** *(Exact links may vary, I used the german steam page :) Let me know if sth. has to be changed)*
- Click on "Load whole match history". This might take 10 minutes.
- After it has finished loading, press CTRL+S to save the page as html-file.
- Now, go to the directory where you saved the CSGOStats.jar. Open the command line (console/cmd/shell) here.
- Run ```java -jar CSGOStats.jar <file-path to the html> <your steam name>```. For me, its: ```java -jar CSGOStats.jar C:\Users\Aaron\Documents\steam_csgo_data.html kthxbye```. You can also replace your name with any other name to get their data (from your recorded matches, at least)

NOTES:
- You need to have java installed to run this.
- If your steam name contains spaces, surround it with quotes ("")!
