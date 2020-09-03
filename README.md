
Description of the app's purpose

This app's purpose was to help users to mantain healthy eyesight when using their phone or computer. This is done by activating a alarm every 20 mins (or the amount of time that the user chooses)
that reminds the user to look up from their device and to look out at something that is more than than 20 metres (or 20 feet) for 20 seconds (the alarm should last 20 seconds unless the user specifies otherwise).

Reason for ceasing development

I stopped development on this project because after enough testing I found out that setExactAndAllowWhileIdle does not work during doze mode which is pretty problematic if the user is using this for a computer or laptop
(This can be avoided if the phone is being used the whole time or if the phone is on charge). Trying to circumvent the doze mode I could use ignore_battery_optimzation but this might be a violation of google play store policies if the google play store deems it unnecessary or damaging to the user experience.
Another problem is the latest api's has been restricting background activity intents.

Known bugs

There are various small bugs which I did not focus on fixing due to the main problem of the alarm not working the way I intended it too. The main bug that was the main reason for ceasing development, was that when the phone was in doze mode the alarm would not activate or would wait until the next wake up peroid which could last way longer than the alarm time. I did not find this bug until later into development because when my phone was being tested it was always plugged in to be charged and I was new to android development. The smaller bugs were to do with screen changes to settings and sound settings such as choosing your own sound and then not selecting anything. There are probably more bugs but I have not worked on this app for some time and I have been not using the app lately.

Next projects

My next projects I am thinking of making is a windows application that is the same as this app, learning new languages/frameworks or a portfolio website.

Note to any viewers

Critiques are welcome.
