# timeSlotbot

### Functionalities
- Ability to send calendar ics -file to bot
- Bot processes and handles ics files
- Bot handles multiple calendars sent by one person and fuses them together
- Bot calculates empty time slots for possible group meetings given specific parameters 
- Usable via simple telegram commands
- Sends Funny Memes 


### User use case example:
Käyttäjä lähettää oman kalenterinsa botille yksityisviestillä. Botti vertailee ryhmän käyttäjien kalentereita ja tulostaa ryhmään kaikki mahdolliset okkousajat.

- Everybody greets the bot with /start -command in group
- Users send .ics files to bot through private messages
- Users use /file command to see if the bot has recieved their files
- One of the users in the group sends /when \<day limit>,\<duration>
- Bot replies with available time slots

### Plan
Read scala doc documentation

### Notice
You need to run the program with administrator privilages
