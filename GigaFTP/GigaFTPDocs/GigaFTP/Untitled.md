#  GigaFtp - Example Java FTP like server

### Requirements
<ul>
	<li>Java: 8</li>
</ul>

### Project files
#### Client
![[Pastedimage20220617134252.png]]
#### Server
![[Pasted image 20220617134225.png]]
![[Pasted image 20220624181139.png]]


<div style="page-break-after: always; visibility: hidden">
\pagebreak
</div>

### Available commands
<ul>
	<li>USER</li>
	<li>PASS</li>
	<li>PORT</li>
	<li>QUIT</li>
	<li>RMD</li>
	<li>MKD</li>
	<li>DELE</li>
	<li><b>RETR</b></li>
	<li><b>STOR with ABOR</b></li>
	<li>CDUP</li>
	<li>CWD</li>
	<li>PWD</li>
	<li>LIST</li>
	<li>RMD</li>
</ul>

	
### Default FTP user and password

User: user
Password: 1234

<b>All users with passwords are loaded from user.config file</b>

<div style="page-break-after: always; visibility: hidden">
\pagebreak
</div>

### Example usage:  Server
Run GigaFTP
All files and subfolders in "public" folder of the server will be hosted

![[Pasted image 20220617132751.png]]

<div style="page-break-after: always; visibility: hidden">
\pagebreak
</div>

### Example usage:  Client

#### Connecting
Run GigaFTPClient and connect to server:

![[Pasted image 20220624180519.png]]

Perform authorization steps:

Type USER and wait for response,
Then type username "user":

![[Pasted image 20220624181312.png]]

Type PASS and wait for response,
Then type password "1234":

![[Pasted image 20220624181318.png]]

Type PORT and wait for response,
Then set Active Mode for port "3020":

![[Pasted image 20220624181332.png]]
<div style="page-break-after: always; visibility: hidden">
\pagebreak
</div>

#### FTP Commands example

Now you can retrieve files correctly, you can use supported commands for example:

PWD:

![[Pasted image 20220617132434.png]]

CWD:
![[Pasted image 20220617132920.png]]

LIST:
![[Pasted image 20220617134102.png]]

RETR:
![[Pasted image 20220617134040.png]]

STOR:

![[Pasted image 20220624180701.png]]
![[Pasted image 20220624180714.png]]

if file does not exists in client directory. 
Client sends ABOR command

![[Pasted image 20220624180820.png]]

QUIT:
![[Pasted image 20220624181049.png]]

#### FTP  test: FTPServer outside Localhost:
![[Pasted image 20220624182651.png]]
![[Pasted image 20220624183056.png]]
![[Pasted image 20220624185638.png]]
Success: Image file is not corrupted:
![[Pasted image 20220624190403.png]]