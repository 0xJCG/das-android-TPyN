<?php

	# - Global variables ----------------------------------------------------------------------------------------------------
	$api_key = "";
	$GCM_url = "https://android.googleapis.com/gcm/send";
	# ------------------------------------------------------------------------------------------------------------------------------

	# - Check POST input ------------------------------------------------------------------------------------------------
	$error = 0;
	if ($_POST){
		$numElem = count($_POST);
		if ($numElem != 1){
			$error = 1;
		}
		else{
			if (!isset($_POST['send'])){
				$error = 1;
			}
		}
	}
	else{
		$error = 1;
	}
	# ------------------------------------------------------------------------------------------------------------------------------

	// Send Message to GCM if no errors: --------------------------------------------------------------------------
	if ($error == 1){
	?>
	<html>
	<body>
		<center>Ha ocurrido un error al enviar el formulario</center>
	</body>
	</html>
	<?php
	}
	else{

			// a. Creamos una conexión a la base de datos:
			$con = new mysqli("localhost", "", "", "");
			// c. Perform queries:
			$emaitza = mysqli_query($con, "SELECT gcm FROM users WHERE gcm IS NOT NULL");
			$ids = array();
			while ($row = mysqli_fetch_array($emaitza)){
				$ids[] = $row['gcm'];	// Atzean gehituz doa
			}
			// d. Close connection:
			mysqli_close($con);


			// 01.- Preparamos la cabecera del mensaje:
			$cabecera = array(
				"Authorization: key=$api_key",
				"Content-Type: application/json"
			);

			// 02.- Preparamos el contenido del mensaje:
			$data = array(
				'Asignatura' => 'DAS',
				'Aplicacion' => 'TPyN'
			);
			$info= array(
				"registration_ids" => $ids,
				"collapse_key" => "TPyN",
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
			$resultado = curl_exec($ch);

			// Cerramos el gestor de Curl:
			curl_close($ch);

echo $resultado;



	}
	# ------------------------------------------------------------------------------------------------------------------------------
?>
