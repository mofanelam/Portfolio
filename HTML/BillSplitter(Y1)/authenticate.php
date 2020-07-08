<?php
session_start();
//Authenticate posts in index.php

include 'database.php';
$db = new Database();

$email = $_POST['email'];
$password = $_POST['password'];

$stmt = $db->prepare("SELECT * FROM users WHERE email =:email");
$stmt->bindValue(':email', $email, SQLITE3_TEXT);

$user = $stmt->execute();


//$user = $db->query("SELECT * FROM users WHERE username='$username';");

while (($row = $user->fetchArray())) {

  if(sha1($row['salt'].$password) == $row['password']) {
    $_SESSION['id'] = $row['user_id'];
    header('Location:all_lists.php');
    } else {
    header('Location:index.php');
  }

}



?>
