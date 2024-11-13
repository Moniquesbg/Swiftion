
CREATE USER 'Penningmeester'@'%' IDENTIFIED BY 'SerieusSterkNietTeKrakenWachtwoord';
CREATE USER 'Gebruiker'@'%' IDENTIFIED BY 'Wachtwoordisnietgebruiker';


-- Privileges for `penningmeester`@`%`
GRANT USAGE ON *.* TO `Penningmeester`@`%`;
GRANT SELECT,UPDATE,DELETE, EXECUTE ON `db_swiftion`.* TO `Penningmeester`@`%`;

-- Privileges for `Gebruiker`@`%`
GRANT SELECT, EXECUTE ON *.* TO `Gebruiker`@`%`;