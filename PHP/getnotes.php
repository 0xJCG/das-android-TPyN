<?php
	$conn = new mysqli("localhost", "", "", "");
	
	// Check connection
	if ($conn->connect_error)
		die(0);
	
	$query = "SELECT * FROM notes ORDER BY NoteCode DESC";
	$sql = $conn->query($query);
	
	$notes = array();

	while($note = $sql->fetch_assoc()) {
		$notes[]['NoteCode'] = $note['NoteCode'];
		$notes[]['NoteTitle'] = $note['NoteTitle'];
		$notes[]['NoteContent'] = $note['NoteContent'];
		$notes[]['NoteLocation'] = $note['NoteLocation'];
		$notes[]['NoteImage'] = $note['NoteImage'];
	}
	
	$conn->close();
	
	header("Content-Type: application/json");
	echo json_encode($notes);
?>