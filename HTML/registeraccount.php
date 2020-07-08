<?php
  session_start();
  include 'database.php';
  $email = $_POST['email'];
  $password = $_POST['password'];
  $username = $_POST['username'];
  $balance = 0.0;

  $salt = sha1(time());
  $encrypted_password = sha1($salt.$password);

  $db = new Database();

  //Need to adjust later for the schema
  $stmt = $db->prepare("INSERT INTO users VALUES (Null, :email, :encryp_pw, :balance, :salt, :username);");
  $stmt->bindValue(':email', $email, SQLITE3_TEXT);
  $stmt->bindValue(':encryp_pw', $encrypted_password, SQLITE3_TEXT);
  $stmt->bindValue(':balance', $balance, SQLITE3_FLOAT);
  $stmt->bindValue(':salt', $salt, SQLITE3_TEXT);
  $stmt->bindValue(':username', $username, SQLITE3_TEXT);

  $results = $stmt->execute();

  //$db->exec("INSERT INTO users VALUES(NULL, '$salt', '$email', '$username', '$encrypted_password');");

  $stmt = $db->prepare("SELECT * FROM users WHERE username=:username");
  $stmt->bindValue(':username', $username, SQLITE3_TEXT);

  $user = $stmt->execute();

  //$user = $db->query("SELECT * FROM users WHERE username='$username';");

  //Automatically logging in a newly registered user
  while (($row = $user->fetchArray())) {
    $_SESSION['id'] = $row['user_id'];
    header('Location:create_groups.php');
  }

?>
