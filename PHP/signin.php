<?php
	$conn = new mysqli("localhost", "", "", "");
	
	// Check connection
	if ($conn->connect_error) {
		header("Content-Type: application/json");
		die(json_encode([]));
	}
	
	if (!empty($_POST["user"]) && !empty($_POST["pass"])) {
		$user = $_POST["user"];
		$pass = $_POST["pass"];
		
		$query = "INSERT INTO users (username, password) VALUES ('$user', '$pass')";
		if (!$conn->query($query)) {
			header("Content-Type: application/json");
			die(json_encode([]));
		} else {
			$query = "INSERT INTO notes (NoteTitle, NoteContent, UserID) VALUES ('Welcome!', 'We hope you enjoy TPyN app!', (SELECT id FROM users WHERE username = '$user'))";
			$conn->query($query);
			
			$query = "SELECT NoteCode, NoteTitle, NoteContent FROM notes WHERE UserID = (SELECT id FROM users WHERE username = '$user')";
			$sql = $conn->query($query);

			$notes = array();

			while($note = $sql->fetch_assoc()) {
				$notes[]['NoteCode'] = $note['NoteCode'];
				$notes[]['NoteTitle'] = $note['NoteTitle'];
				$notes[]['NoteContent'] = $note['NoteContent'];
			}

			$conn->close();

			header("Content-Type: application/json");
			echo json_encode($notes);
			}
	} else {
		header("Content-Type: application/json");
		echo json_encode([]);
	}
?>