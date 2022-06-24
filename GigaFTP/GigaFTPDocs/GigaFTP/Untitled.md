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
	<li>RETR></li>
	<li>CDUP</li>
	<li>CWD</li>
	<li>PWD</li>
	<li>LIST</li>
</ul>

	
### Default FTP user and password

User: user
Password: 1234

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
Run GigaFTPClient and perform authorization steps:

Type USER and wait for response,
Then type username "user":
![[Pasted image 20220617131921.png]]

Type PASS and wait for response,
Then type password "1234":

![[Pasted image 20220617132040.png]]

Type PORT and wait for response,
Then set Active Mode for port "3020":

![[Pasted image 20220617132156.png]]

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

QUIT:
![[Pasted image 20220617134131.png]]