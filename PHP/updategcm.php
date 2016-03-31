<?php
	$conn = new mysqli("localhost", "", "", "");
	
	// Check connection
	if ($conn->connect_error)
		die(0);
	
	if (!empty($_POST["user"]) && !empty($_POST["gcm"])) {
		$user = $_POST["user"];
		$gcm = $_POST["gcm"];
		
		$query = "UPDATE users SET gcm = '$gcm' WHERE username = '$user'";
		$sql = $conn->query($query);
		$conn->close();
		
		# - Global variables ----------------------------------------------------------------------------------------------------
		$api_key = "";
		$GCM_url = "https://android.googleapis.com/gcm/send";
		# -----------------------------------------------------------------------------------------------------------------------
		
		$ids = array();
		$ids[] = $gcm;
		
		// 01.- Preparamos la cabecera del mensaje:
		$cabecera = array(
			"Authorization: key=$api_key",
			"Content-Type: application/json"
		);

		// 02.- Preparamos el contenido del mensaje:
		$data = array(
			'TPyN' => 'Probando PUSH'
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
		
		echo 1;
	} else
		echo 0;
?>