<?php
	$conn = new mysqli("localhost", "", "", "");
	
	// Check connection
	if ($conn->connect_error)
		die(0);
	
	if (!empty($_POST["title"]) && !empty($_POST["content"]) && !empty($_POST["user"])) {
		$title = $_POST["title"];
		$content = $_POST["content"];
		$user = $_POST["user"];
		
		$query = "INSERT INTO notes (NoteTitle, NoteContent, UserID) VALUES ('$title', '$content', (SELECT id FROM users WHERE username = '$user'))";
		$sql = $conn->query($query);
		$conn->close();
		
		echo 1;
	} else
		echo 0;
?>