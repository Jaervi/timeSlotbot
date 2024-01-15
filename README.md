# timeSlotbot

### Short overview
TimeSlotbot is a telegram bot designed to make comparing calendars and free timeslots between people easier. The bot works with .ics files, a common calendar format, and sends a textual message with the desired information. The program was made during a programming studio course. The language of the project is Scala.

### Functionalities
- Ability to send calendar .ics -file to bot
- Bot processes and handles sent .ics files
- Bot processes multiple calendars sent by one person and fuses them together
- Bot calculates empty time slots given specific parameters 
- Usable via simple telegram commands
- Sends Funny Memes


### User use case example
The user sends their calendar to the bot via a private message. The bot compares and processes the calendars sent by all the group members (also via a private message) and then sends a message with all the free time slots.

- Everybody greets the bot with /start -command in group
- Users send .ics files to bot through private messages
- Users use /file command to see if the bot has recieved their files
- One of the users in the group sends /when \<day limit>,\<duration>
- Bot replies with available time slots


### Notice
You need to run the program with administrator privilages
