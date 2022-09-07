
DELIMITER $$
DROP PROCEDURE IF EXISTS insertLoop$$

CREATE PROCEDURE insertLoop()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 400000 DO
SELECT LEFT(UUID(), 8) INTO @user_id;
INSERT INTO `database`.test(user_id)
VALUES (@user_id);
SET i = i + 1;
END WHILE;
END$$
DELIMITER $$

CALL insertLoop;
$$