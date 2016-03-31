<!DOCTYPE html>
<html lang="es" manifest="juego.manifest">
	<head>
		<meta charset="UTF-8">
		<title>User administration</title>
		<link rel="stylesheet" href="admin_users.css">
	</head>
<?php
	if ($_POST['push']) {
		# - Global variables ----------------------------------------------------------------------------------------------------
		$api_key = "";
		$GCM_url = "https://android.googleapis.com/gcm/send";
		# -----------------------------------------------------------------------------------------------------------------------
		
		$ids = array();
		$ids[] = $_POST['push'];
		
		// 01.- Preparamos la cabecera del mensaje:
		$cabecera = array(
			"Authorization: key=$api_key",
			"Content-Type: application/json"
		);

		// 02.- Preparamos el contenido del mensaje:
		$data = array(
			'TPyN' => 'Sending PUSH'
		);
		$info= array(
			"registration_ids" => $ids,
			"collapse_key" => "Examen",
			"time_to_live" => 200,
			"data" => $data
		);

		// Inicializamos el gestor de curl:
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, $GCM_url);

		// Configuramos el mensaje:
		curl_setopt($ch, CURLOPT_HTTPHEADER, $cabecera);
		curl_setopt($ch, CURLOPT_POST, true);
		curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($info));
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

		// Enviamos el mensaje:
		curl_exec($ch);

		// Cerramos el gestor de Curl:
		curl_close($ch);
	}
	
	if ($_POST['delete']) {
		$conn = new mysqli("localhost", "", "", "");
		
		$query = "DELETE FROM users WHERE id = " . $_POST['delete'];
		$sql = $conn->query($query);
		
		$conn->close();
	}
	
	if ($_POST['add']) {
		$conn = new mysqli("localhost", "", "", "");
		
		$user = $_POST['username'];
		$pass = hash('sha512', $_POST['password']);
		$gcm = $_POST['gcm'];
		
		if (trim($gcm) === "")
			$query = "INSERT INTO users (username, password) VALUES('$user', '$pass')";
		else
			$query = "INSERT INTO users (username, password, gcm) VALUES('$user', '$pass', '$gcm')";
		
		$sql = $conn->query($query);
		
		$conn->close();
	} else if ($_POST['edit']) {
		$conn = new mysqli("localhost", "", "", "");
		
		$id = $_POST['edit'];
		$query = "SELECT id, username, gcm FROM users WHERE id = '$id'";
		
		$sql = $conn->query($query);
		$user_data = mysqli_fetch_array($sql);
		
		$conn->close();
		
		$edit = true;
	} else if ($_POST['update']) {
		$conn = new mysqli("localhost", "", "", "");
		
		$id = $_POST['update'];
		$user = $_POST['username'];
		$pass = hash('sha512', $_POST['password']);
		$gcm = $_POST['gcm'];
		
		if (trim($gcm) === "")
			$query = "UPDATE users SET username = '$user', password = '$pass', gcm = NULL WHERE id = $id";
		else
			$query = "UPDATE users SET username = '$user', password = '$pass', gcm = '$gcm' WHERE id = $id";
		
		$sql = $conn->query($query);
		
		$user_data = mysqli_fetch_array($sql);
		
		$conn->close();
		
		$edit = true;
	}
?>
	<body>
		<table class="CSSTableGenerator">
			<caption>User administration</caption>
			<tr>
				<th>Username</th>
				<th>GCM registered</th>
				<th colspan="3">Actions</th>
			</tr>
<?php
	$conn = new mysqli("localhost", "", "", "");
	
	// Check connection
	if ($conn->connect_error)
		die(0);
	
	$query = "SELECT * FROM users";
	$sql = $conn->query($query);
	
	$users = array();
	
	while($user = $sql->fetch_assoc()) {
		echo "\t\t\t" . '<tr>' . "\n";
		echo "\t\t\t\t" . '<td>' . $user['username'] . '</td>' . "\n";
		if (is_null($user['gcm']))
			echo "\t\t\t\t" . '<td>No</td>' . "\n";
		else
			echo "\t\t\t\t" . '<td>Yes</td>' . "\n";
		echo "\t\t\t\t" . '<td class="center"><form action="" method="POST"><input type="hidden" name="edit" value="' . $user['id'] . '" /><input class="button1" type="submit" value="Edit" /></form></td>' . "\n";
		echo "\t\t\t\t" . '<td class="center"><form action="" method="POST"><input type="hidden" name="delete" value="' . $user['id'] . '" /><input class="button1" type="submit" value="Delete" /></form></td>' . "\n";
		if (is_null($user['gcm']))
			echo "\t\t\t\t" . '<td class="center"></td>' . "\n";
		else
			echo "\t\t\t\t" . '<td class="center"><form action="" method="POST"><input type="hidden" name="push" value="' . $user['gcm'] . '" /><input class="button1" type="submit" value="Send push" /></form></td>' . "\n";
		echo "\t\t\t" . "</tr>" . "\n";
	}
	
	$conn->close();
?>
		</table>
		<form action="" method="POST" class="dark-matter">
			<label>
				<span>Username:</span>
				<input type="text" name="username" value="<?php echo ($edit) ? $user_data['username'] : ""; ?>" />
			</label>
			<label>
				<span>Password:</span>
				<input type="password" name="password" />
			</label>
			<label>
				<span>GCM ID:</span>
				<input type="text" name="gcm" value="<?php echo ($edit) ? $user_data['gcm'] : ""; ?>" />
			</label>
			<input type="hidden" name="<?php echo ($edit) ? "update" : "add"; ?>" value="<?php echo ($edit) ? $user_data['id'] : 1; ?>" />
			<label>
				<span>&nbsp;</span>
				<input class="button" type="submit" value="<?php echo ($edit) ? "Update user" : "Add user"; ?>" />
			</label>
		</form>
	</body>
</html>