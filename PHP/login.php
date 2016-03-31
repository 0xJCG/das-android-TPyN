<?php
	$conn = new mysqli("localhost", "", "", "");
	
	// Check connection
	if ($conn->connect_error)
		die(0);
	
	if (!empty($_POST["user"]) && !empty($_POST["pass"])) {
		$user = $_POST["user"];
		$pass = $_POST["pass"];
		
		$query = "SELECT username FROM users WHERE username = '$user' AND password = '$pass'";
		
		$sql = $conn->query($query);
		$row = mysqli_fetch_array($sql);
		
		if (!empty($row)) {
			$query = "SELECT username FROM users WHERE username = '$user' AND password = '$pass'";
			$sql = $conn->query($query);
			
			$query = "SELECT NoteCode, NoteTitle, NoteContent FROM notes WHERE UserID = (SELECT id FROM users WHERE username = '$user') ORDER BY NoteCode DESC";
			$sql = $conn->query($query);
			
			$notes = array();

			while ($note = $sql->fetch_assoc()) {
				$note_array['NoteCode'] = $note['NoteCode'];
				$note_array['NoteTitle'] = $note['NoteTitle'];
				$note_array['NoteContent'] = $note['NoteContent'];
				array_push($notes, $note_array);
			}
			
			/*while($note = $sql->fetch_assoc()) {
				$notes[]['NoteCode'] = $note['NoteCode'];
				$notes[]['NoteTitle'] = $note['NoteTitle'];
				$notes[]['NoteContent'] = $note['NoteContent'];
			}*/
			
			header("Content-Type: application/json");
			echo json_encode($notes);//, JSON_FORCE_OBJECT);
		} else
			echo 0;
			
		$conn->close();
	} else
		echo 0;
?>