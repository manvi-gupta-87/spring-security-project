insert into users(username, password, enabled) values
('user',    '$2a$10$YrUNg4jWGVWViWIRG2cqW.dsD/Hhu77dQps46dkvb39uwQkPOzYg.', true),
('admin',   '$2a$10$MWP/L0GCuNki6llpnLPVNu.XTX5riNB7dhJScCrgV3nlA19C8XRi6', true),
('manager',  '$2a$10$EnZQwdHr9DwPYBl7pvg6U.HBHKlt//9/PbV4EzdleiM9Cab7WnvzK', true);

insert into authorities(username, authority) values
('user', 'ROLE_USER'),
('admin','ROLE_ADMIN'),
('manager', 'ROLE_MANAGER');