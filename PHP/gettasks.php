<?php
	$conn = new mysqli("localhost", "", "", "");
	
	// Check connection
	if ($conn->connect_error)
		die(0);
	
	$query = "SELECT * FROM tasks ORDER BY TaskCode DESC";
	$sql = $conn->query($query);
	
	$tasks = array();

	while($task = $sql->fetch_assoc()) {
		$tasks[]['TaskCode'] = $task['TaskCode'];
		$tasks[]['TaskTitle'] = $task['TaskTitle'];
		$tasks[]['TaskContent'] = $task['TaskContent'];
		$tasks[]['TaskWhen'] = $task['TaskWhen'];
		$tasks[]['TaskWhere'] = $task['TaskWhere'];
	}
	
	$conn->close();
	
	header("Content-Type: application/json");
	echo json_encode($tasks);
?>