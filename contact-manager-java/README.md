# Simple Contact Online Management (Plain Java)

A web-based address book for personal and professional contacts.
Pure Java: uses only the JDK's built-in web server. No Maven, no Spring, no downloads.

## Features
- Add, view, edit and delete contacts
- Stores name, phone number, email and category (Personal / Professional)
- Listing of all contacts with saved details
- Mark / unmark a favourite (click the star)
- Separate Favourites list
- Optional: search by name or phone number
- Input validation; data saved to contacts.txt (survives restarts)

## Requirements
JDK 11 or newer (check with: javac -version)

## Run
Windows: double-click run.bat   (or type .\run.bat in a terminal)
Mac/Linux: ./run.sh

Manual way:
    mkdir out
    javac -encoding UTF-8 -d out src/*.java
    java -cp out Main

Then open http://localhost:8080

## Run in VS Code
Open this folder, open a terminal and run the commands above.
Or open src/Main.java and click "Run" above main().

## Files
    src/Main.java          web server + routes + validation
    src/Contact.java       contact model
    src/ContactStore.java  in-memory list + file saving + search
    src/HtmlPages.java     HTML pages
