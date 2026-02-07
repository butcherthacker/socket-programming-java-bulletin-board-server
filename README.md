# socket-programming-java-bulletin-board-server
A multi-threaded networked bulletin board system implemented with Java TCP sockets.

## Overview
This project consists of two components:
- **Server**: Multi-threaded TCP server that manages a shared bulletin board
- **GUI Client**: Java Swing graphical interface for interacting with the server

## Prerequisites
- **Java Development Kit (JDK)** version 8 or higher
- **Java Runtime Environment (JRE)**
- No external libraries required (uses standard Java SE and Swing)

Verify your installation:
```powershell
java -version
javac -version
```

## Project Structure
```
socket-programming-java-bulletin-board-server/
├── Server/                    # Server implementation
│   ├── BulletinBoardServer.java
│   ├── BoardState.java
│   ├── ClientConnection.java
│   └── Note.java
├── GUI/                       # GUI Client implementation
│   ├── BBoardClient.java
│   ├── ClientUI.java
│   ├── ProtocolClient.java
│   ├── Handshake.java
│   ├── Response.java
│   ├── OkResponse.java
│   ├── OkNoteResponse.java
│   ├── ErrorResponse.java
│   ├── NotesListResponse.java
│   ├── PinsListResponse.java
│   ├── NoteRecord.java
│   └── PinRecord.java
└── README.md
```

## Compilation

### Compile the Server
```powershell
cd Server
javac *.java
```

### Compile the GUI Client
```powershell
cd GUI
javac *.java
```

## Running the Application

### Step 1: Start the Server

Open a terminal/command prompt and run:

```powershell
cd Server
java BulletinBoardServer <port> <board_width> <board_height> <note_width> <note_height> <colour1> ... <colourN>
```

**Parameters:**
- `port` - Port number (1-65535)
- `board_width` - Width of the bulletin board (positive integer)
- `board_height` - Height of the bulletin board (positive integer)
- `note_width` - Width of each note (positive integer)
- `note_height` - Height of each note (positive integer)
- `colour1...N` - At least one colour name (space-separated)

**Example:**
```powershell
java BulletinBoardServer 4321 200 100 20 10 red white green yellow blue
```

This starts the server on port **4321** with:
- Board dimensions: 200×100
- Note dimensions: 20×10
- Available colours: red, white, green, yellow, blue

The server will display no output but is running and listening for connections.

### Step 2: Launch the GUI Client

Open a **new terminal/command prompt** and run:

```powershell
cd GUI
java BBoardClient
```

A window titled "Bulletin Board Client" will open.

### Step 3: Connect to the Server

1. In the GUI **Connection** panel:
   - **Host**: Enter `127.0.0.1` (for local server)
   - **Port**: Enter `4321` (or whatever port your server is using)
2. Click **Connect**

**Expected Result:**
- Status changes to "Connected" (green)
- Output area shows the HELLO message with board configuration
- Colour dropdowns populate with the server's available colours
- All command buttons become enabled

## Using the GUI Client

The client has four main tabs for different operations:

### POST Tab - Create Notes
1. Enter **X** and **Y** coordinates (non-negative integers)
2. Select a **Colour** from the dropdown
3. Enter a **Message** (optional)
4. Click **POST Note**

**Expected Response:** `OK NOTE <note_id>`

### GET Tab - Query Notes and Pins

#### Get All Notes:
- Click **GET Notes** (no filters checked)

#### Filter Notes:
- **By Colour**: Check "Filter by colour", select colour, click **GET Notes**
- **By Contains**: Check "Filter by contains", enter X and Y coordinates, click **GET Notes**
- **By Refers To**: Check "Filter by refersTo", enter search term (single word), click **GET Notes**
- Filters can be combined

#### Get All Pins:
- Click **GET PINS**

**Expected Response:** List of notes/pins with details formatted in columns

### PIN/UNPIN Tab - Manage Pins
1. Enter **X** and **Y** coordinates
2. Click **PIN** to pin a note at that location
3. Click **UNPIN** to unpin a note at that location

**Expected Response:** `OK`

### Management Tab - Board Operations

**SHAKE (Remove Unpinned):**
- Removes all unpinned notes from the board
- Click **SHAKE (Remove Unpinned)**

**CLEAR (Remove All):**
- Removes all notes AND all pins from the board
- Click **CLEAR (Remove All)**

**Expected Response:** `OK`

## Testing Workflow

### Basic Test Scenario

1. **Start Server** (see Step 1 above)

2. **Launch and Connect Client**
   ```powershell
   cd GUI
   java BBoardClient
   ```
   - Connect to `127.0.0.1:4321`
   - Verify HELLO message appears
   - Verify colours populate dropdowns

3. **Test POST**
   - POST tab: X=`0`, Y=`0`, Colour=`red`, Message=`Hello World`
   - Verify: `OK NOTE 0` (or similar ID)

4. **Test GET Notes**
   - GET tab: Click **GET Notes**
   - Verify: Table shows the note you posted
   - Test colour filter: Check "Filter by colour", select `red`, click **GET Notes**
   - Verify: Only red notes shown

5. **Test PIN**
   - PIN/UNPIN tab: X=`0`, Y=`0`, click **PIN**
   - Verify: `OK`
   - GET tab: Click **GET PINS**
   - Verify: Shows `PIN 0 0`

6. **Test UNPIN**
   - PIN/UNPIN tab: X=`0`, Y=`0`, click **UNPIN**
   - Verify: `OK`
   - GET tab: Click **GET PINS**
   - Verify: No pins shown

7. **Test Multiple Notes**
   - POST several notes at different positions with different colours
   - Use GET with various filters to verify correct filtering

8. **Test SHAKE**
   - POST some notes (do NOT pin them)
   - POST other notes and PIN them
   - Management tab: Click **SHAKE**
   - GET Notes: Verify unpinned notes removed, pinned notes remain

9. **Test CLEAR**
   - Management tab: Click **CLEAR**
   - GET Notes: Verify board is empty
   - GET PINS: Verify no pins

10. **Test Disconnect**
    - Click **Disconnect**
    - Verify: Status shows "Not connected" (red)
    - Verify: All command buttons disabled

### Error Testing

**Out of Bounds:**
- Try POST with X or Y beyond board dimensions
- Expected: `ERROR OUT_OF_BOUNDS`

**Overlap:**
- POST two notes at the same coordinates
- Expected: `ERROR OVERLAP_ERROR`

**Invalid Colour:**
- This is prevented by the GUI (dropdown only shows valid colours)

**No Note at Coordinate:**
- Try to PIN/UNPIN at coordinates with no note
- Expected: `ERROR NO_NOTE_AT_COORDINATE` or `ERROR PIN_NOT_FOUND`

## Protocol Details

### Connection Handshake
On connect, server sends:
```
HELLO <board_w> <board_h> <note_w> <note_h> COLOURS <k> <colour1> ... <colourk>
```

### Commands
- `POST <x> <y> <colour> <message>`
- `GET` (with optional filters: `colour=<c>`, `contains=<x> <y>`, `refersTo=<s>`)
- `GET PINS`
- `PIN <x> <y>`
- `UNPIN <x> <y>`
- `SHAKE`
- `CLEAR`
- `DISCONNECT`

### Responses
- `OK` - Simple success
- `OK NOTE <id>` - POST success with note ID
- `OK <count>` - Multi-line list follows, terminated with `END`
- `ERROR <CODE> <description>` - Error occurred

## GUI Features

✅ **Non-blocking UI** - All network operations run off the Event Dispatch Thread  
✅ **Concurrent request prevention** - Buttons disable during operations  
✅ **Graceful disconnect handling** - Detects server disconnection  
✅ **Input validation** - Validates coordinates, colours, and filter values  
✅ **Formatted output** - Clear display of responses in tabular format  
✅ **Server-driven configuration** - Colour dropdowns populated from server HELLO  

## Troubleshooting

**Server won't start:**
- Ensure port is not already in use
- Try a different port number
- Check that you're in the `Server` directory

**GUI can't connect:**
- Verify server is running
- Check host and port match server configuration
- Ensure no firewall blocking connection

**Class not found error:**
- Make sure you compiled the files: `javac *.java`
- Make sure you're in the correct directory when running `java` command

**GUI freezes:**
- This should not happen - all network operations are asynchronous
- If it does, report as a bug

## Notes

- The server supports multiple concurrent clients
- Each client operates independently
- The board state is shared and synchronized across all clients
- The GUI client prevents concurrent requests (buttons disable during operations)
- Use `CTRL+C` in the server terminal to stop the server
