-- Users table
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       email VARCHAR(255),
                       department VARCHAR(100),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       role_name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Resources table (SSH, commands, endpoints, etc.)
CREATE TABLE resources (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           resource_name VARCHAR(100) NOT NULL UNIQUE,
                           resource_type VARCHAR(50) NOT NULL, -- 'SSH', 'COMMAND', 'ENDPOINT', etc.
                           description VARCHAR(255),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Actions table
CREATE TABLE actions (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         action_name VARCHAR(50) NOT NULL UNIQUE, -- 'EXECUTE', 'READ', 'WRITE', 'DELETE'
                         description VARCHAR(255),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Policies table (ABAC rules stored in database)
CREATE TABLE policies (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          policy_name VARCHAR(100) NOT NULL,
                          role_id BIGINT,
                          resource_id BIGINT,
                          action_id BIGINT,
                          effect VARCHAR(10) NOT NULL DEFAULT 'ALLOW', -- 'ALLOW' or 'DENY'
                          conditions JSON, -- Store additional conditions like time, IP, etc.
                          priority INT DEFAULT 0, -- Higher priority = evaluated first
                          enabled BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (role_id) REFERENCES roles(id),
                          FOREIGN KEY (resource_id) REFERENCES resources(id),
                          FOREIGN KEY (action_id) REFERENCES actions(id)
);

-- User-Role assignment table
CREATE TABLE user_roles (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (role_id) REFERENCES roles(id),
                            UNIQUE KEY unique_user_role (user_id, role_id)
);

-- Sample data
INSERT INTO users (username, email, department) VALUES
                                                    ('john_developer', 'john@example.com', 'IT'),
                                                    ('jane_operator', 'jane@example.com', 'Operations'),
                                                    ('admin_user', 'admin@example.com', 'IT');

INSERT INTO roles (role_name, description) VALUES
                                               ('ADMIN', 'Administrator with full access'),
                                               ('DEVELOPER', 'Developer role'),
                                               ('OPERATOR', 'System operator role');

INSERT INTO resources (resource_name, resource_type, description) VALUES
                                                                      ('SSH', 'SERVICE', 'SSH service access'),
                                                                      ('rm', 'COMMAND', 'Remove command'),
                                                                      ('ls', 'COMMAND', 'List command'),
                                                                      ('cat', 'COMMAND', 'Cat command'),
                                                                      ('sudo', 'COMMAND', 'Sudo command');

INSERT INTO actions (action_name, description) VALUES
                                                   ('EXECUTE', 'Execute action'),
                                                   ('READ', 'Read action'),
                                                   ('WRITE', 'Write action'),
                                                   ('DELETE', 'Delete action');

INSERT INTO user_roles (user_id, role_id) VALUES
                                              (1, 2), -- john_developer is DEVELOPER
                                              (2, 3), -- jane_operator is OPERATOR
                                              (3, 1); -- admin_user is ADMIN

-- Policies: Example - Developer can SSH but cannot use rm command
INSERT INTO policies (policy_name, role_id, resource_id, action_id, effect, priority) VALUES
                                                                                          ('Allow SSH for Developer', 2, 1, 1, 'ALLOW', 10), -- DEVELOPER can EXECUTE SSH
                                                                                          ('Deny rm for Developer', 2, 2, 1, 'DENY', 20),     -- DEVELOPER cannot EXECUTE rm
                                                                                          ('Allow ls for Developer', 2, 3, 1, 'ALLOW', 10),  -- DEVELOPER can EXECUTE ls
                                                                                          ('Allow all for Admin', 1, NULL, NULL, 'ALLOW', 5); -- ADMIN can do everything (NULL = all resources/actions)