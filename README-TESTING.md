# Testing Guide - Drools ABAC Access Control System

This document provides comprehensive testing scenarios and flows for the Drools-based Attribute-Based Access Control (ABAC) system.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [System Overview](#system-overview)
- [Test Data Setup](#test-data-setup)
- [API Endpoints](#api-endpoints)
- [Test Scenarios](#test-scenarios)
- [Flow Diagrams](#flow-diagrams)
- [Expected Results Matrix](#expected-results-matrix)
- [Testing Commands](#testing-commands)

## 🔧 Prerequisites

1. **Database Setup**
   - MariaDB running (via Docker Compose)
   - Database tables created (execute `src/main/resources/sql/mysql.sql`)
   - Sample data inserted

2. **Application Running**
   - Spring Boot application started on port `8081`
   - Environment variables configured (`.env` file)

3. **Tools**
   - Postman, cURL, or any HTTP client
   - Database client (optional, for verification)

## 🎯 System Overview

### Architecture Flow

```
Client Request → Controller → Service → Repository → Database
                                    ↓
                              Drools Engine
                                    ↓
                              Policy Evaluation
                                    ↓
                              Access Decision
```

### Components

1. **Users**: `john_developer`, `jane_operator`, `admin_user`
2. **Roles**: `ADMIN`, `DEVELOPER`, `OPERATOR`
3. **Resources**: `SSH`, `rm`, `ls`, `cat`, `sudo`
4. **Actions**: `EXECUTE`, `READ`, `WRITE`, `DELETE`
5. **Policies**: Rules stored in database, evaluated by Drools

## 📊 Test Data Setup

### Users and Roles

| Username | Role | User ID | Role ID |
|----------|------|---------|---------|
| `john_developer` | DEVELOPER | 1 | 2 |
| `jane_operator` | OPERATOR | 2 | 3 |
| `admin_user` | ADMIN | 3 | 1 |

### Resources

| Resource Name | Type | Resource ID |
|---------------|------|-------------|
| SSH | SERVICE | 1 |
| rm | COMMAND | 2 |
| ls | COMMAND | 3 |
| cat | COMMAND | 4 |
| sudo | COMMAND | 5 |

### Actions

| Action Name | Action ID |
|-------------|-----------|
| EXECUTE | 1 |
| READ | 2 |
| WRITE | 3 |
| DELETE | 4 |

### Policies (Rules)

| Policy Name | Role | Resource | Action | Effect | Priority |
|-------------|------|----------|--------|--------|----------|
| Allow SSH for Developer | DEVELOPER | SSH | EXECUTE | ALLOW | 10 |
| Deny rm for Developer | DEVELOPER | rm | EXECUTE | DENY | 20 |
| Allow ls for Developer | DEVELOPER | ls | EXECUTE | ALLOW | 10 |
| Allow all for Admin | ADMIN | NULL | NULL | ALLOW | 5 |

## 🌐 API Endpoints

### 1. Generic Access Check

**Endpoint:** `POST /api/access/check`

**Request Body:**
```json
{
  "username": "john_developer",
  "resourceName": "rm",
  "actionName": "EXECUTE",
  "resourceType": "COMMAND"
}
```

**Response:**
```json
{
  "allowed": false,
  "message": "NOT ALLOWED",
  "username": "john_developer",
  "resourceName": "rm",
  "actionName": "EXECUTE"
}
```

### 2. SSH Access Check

**Endpoint:** `POST /api/access/ssh?username={username}`

**Example:**
```
POST http://localhost:8081/api/access/ssh?username=john_developer
```

**Response:**
```json
{
  "allowed": true,
  "message": "ALLOWED",
  "username": "john_developer",
  "resourceName": "SSH",
  "actionName": "EXECUTE"
}
```

### 3. Command Access Check

**Endpoint:** `POST /api/access/command?username={username}&command={command}`

**Example:**
```
POST http://localhost:8081/api/access/command?username=john_developer&command=rm
```

**Response:**
```json
{
  "allowed": false,
  "message": "NOT ALLOWED",
  "username": "john_developer",
  "resourceName": "rm",
  "actionName": "EXECUTE"
}
```

## 🧪 Test Scenarios

### Scenario 1: Developer - SSH Access

**Test Case:** Developer user trying to access SSH service

**Request:**
```bash
POST /api/access/ssh?username=john_developer
```

**Expected Flow:**
1. Controller receives request
2. Service loads user `john_developer` from database
3. Service loads role `DEVELOPER` (role_id = 2)
4. Service loads resource `SSH` (resource_id = 1)
5. Service loads action `EXECUTE` (action_id = 1)
6. Service loads policies for DEVELOPER role
7. Drools evaluates: Policy "Allow SSH for Developer" matches
8. **Result: ALLOWED**

**Expected Response:**
```json
{
  "allowed": true,
  "message": "ALLOWED",
  "username": "john_developer",
  "resourceName": "SSH",
  "actionName": "EXECUTE"
}
```

---

### Scenario 2: Developer - rm Command (Denied)

**Test Case:** Developer user trying to execute `rm` command

**Request:**
```bash
POST /api/access/command?username=john_developer&command=rm
```

**Expected Flow:**
1. Controller receives request
2. Service loads user `john_developer` → DEVELOPER role
3. Service loads resource `rm` (resource_id = 2)
4. Service loads action `EXECUTE`
5. Service loads policies: "Deny rm for Developer" (priority 20)
6. Drools evaluates: DENY policy has higher priority
7. **Result: NOT ALLOWED**

**Expected Response:**
```json
{
  "allowed": false,
  "message": "NOT ALLOWED",
  "username": "john_developer",
  "resourceName": "rm",
  "actionName": "EXECUTE"
}
```

---

### Scenario 3: Developer - ls Command (Allowed)

**Test Case:** Developer user trying to execute `ls` command

**Request:**
```bash
POST /api/access/command?username=john_developer&command=ls
```

**Expected Flow:**
1. User: `john_developer` → Role: DEVELOPER
2. Resource: `ls` (resource_id = 3)
3. Action: `EXECUTE`
4. Policy: "Allow ls for Developer" matches
5. **Result: ALLOWED**

**Expected Response:**
```json
{
  "allowed": true,
  "message": "ALLOWED",
  "username": "john_developer",
  "resourceName": "ls",
  "actionName": "EXECUTE"
}
```

---

### Scenario 4: Developer - cat Command (No Policy - Default Deny)

**Test Case:** Developer user trying to execute `cat` command (no explicit policy)

**Request:**
```bash
POST /api/access/command?username=john_developer&command=cat
```

**Expected Flow:**
1. User: `john_developer` → Role: DEVELOPER
2. Resource: `cat` (resource_id = 4)
3. Action: `EXECUTE`
4. No matching policy found
5. Drools default deny rule applies
6. **Result: NOT ALLOWED**

**Expected Response:**
```json
{
  "allowed": false,
  "message": "NOT ALLOWED",
  "username": "john_developer",
  "resourceName": "cat",
  "actionName": "EXECUTE"
}
```

---

### Scenario 5: Admin - Full Access

**Test Case:** Admin user trying to access any resource

**Request:**
```bash
POST /api/access/command?username=admin_user&command=rm
```

**Expected Flow:**
1. User: `admin_user` → Role: ADMIN
2. Resource: `rm`
3. Action: `EXECUTE`
4. Drools rule: "Admin has full access" matches
5. **Result: ALLOWED** (regardless of resource)

**Expected Response:**
```json
{
  "allowed": true,
  "message": "ALLOWED",
  "username": "admin_user",
  "resourceName": "rm",
  "actionName": "EXECUTE"
}
```

**Test Admin with SSH:**
```bash
POST /api/access/ssh?username=admin_user
```
**Expected:** `allowed: true`

**Test Admin with sudo:**
```bash
POST /api/access/command?username=admin_user&command=sudo
```
**Expected:** `allowed: true`

---

### Scenario 6: Operator - No Policies (Default Deny)

**Test Case:** Operator user trying to access SSH (no policies defined)

**Request:**
```bash
POST /api/access/ssh?username=jane_operator
```

**Expected Flow:**
1. User: `jane_operator` → Role: OPERATOR
2. Resource: `SSH`
3. Action: `EXECUTE`
4. No policies defined for OPERATOR role
5. Default deny applies
6. **Result: NOT ALLOWED**

**Expected Response:**
```json
{
  "allowed": false,
  "message": "NOT ALLOWED",
  "username": "jane_operator",
  "resourceName": "SSH",
  "actionName": "EXECUTE"
}
```

---

### Scenario 7: Non-existent User

**Test Case:** User that doesn't exist in database

**Request:**
```bash
POST /api/access/ssh?username=nonexistent_user
```

**Expected Flow:**
1. Service tries to find user `nonexistent_user`
2. User not found in database
3. Service returns empty Mono
4. `switchIfEmpty` triggers deny response
5. **Result: NOT ALLOWED**

**Expected Response:**
```json
{
  "allowed": false,
  "message": "NOT ALLOWED",
  "username": "nonexistent_user",
  "resourceName": "SSH",
  "actionName": "EXECUTE"
}
```

---

### Scenario 8: Non-existent Resource

**Test Case:** Valid user trying to access non-existent resource

**Request:**
```bash
POST /api/access/command?username=john_developer&command=nonexistent_command
```

**Expected Flow:**
1. User found: `john_developer` → DEVELOPER
2. Resource `nonexistent_command` not found
3. Service returns empty Mono
4. **Result: NOT ALLOWED** (or error)

**Expected Response:**
```json
{
  "allowed": false,
  "message": "NOT ALLOWED",
  "username": "john_developer",
  "resourceName": "nonexistent_command",
  "actionName": "EXECUTE"
}
```

---

### Scenario 9: Policy Priority Test

**Test Case:** Multiple policies with different priorities

**Note:** Currently, DENY policies have higher priority (20) than ALLOW policies (10). This means DENY will be evaluated first.

**Request:**
```bash
POST /api/access/command?username=john_developer&command=rm
```

**Expected Flow:**
1. Policies loaded and sorted by priority DESC
2. DENY policy (priority 20) evaluated first
3. If DENY matches, access is denied
4. If DENY doesn't match, ALLOW policies evaluated
5. For `rm` command: DENY policy matches → **NOT ALLOWED**

---

### Scenario 10: Generic Check Endpoint

**Test Case:** Using the generic `/check` endpoint

**Request:**
```bash
POST /api/access/check
Content-Type: application/json

{
  "username": "john_developer",
  "resourceName": "ls",
  "actionName": "EXECUTE",
  "resourceType": "COMMAND"
}
```

**Expected Response:**
```json
{
  "allowed": true,
  "message": "ALLOWED",
  "username": "john_developer",
  "resourceName": "ls",
  "actionName": "EXECUTE"
}
```

## 📈 Expected Results Matrix

| User | Role | Resource | Action | Expected Result | Policy Matched |
|------|------|----------|--------|----------------|----------------|
| john_developer | DEVELOPER | SSH | EXECUTE | ✅ ALLOWED | Allow SSH for Developer |
| john_developer | DEVELOPER | rm | EXECUTE | ❌ NOT ALLOWED | Deny rm for Developer |
| john_developer | DEVELOPER | ls | EXECUTE | ✅ ALLOWED | Allow ls for Developer |
| john_developer | DEVELOPER | cat | EXECUTE | ❌ NOT ALLOWED | Default deny |
| john_developer | DEVELOPER | sudo | EXECUTE | ❌ NOT ALLOWED | Default deny |
| admin_user | ADMIN | SSH | EXECUTE | ✅ ALLOWED | Admin full access |
| admin_user | ADMIN | rm | EXECUTE | ✅ ALLOWED | Admin full access |
| admin_user | ADMIN | ls | EXECUTE | ✅ ALLOWED | Admin full access |
| admin_user | ADMIN | cat | EXECUTE | ✅ ALLOWED | Admin full access |
| admin_user | ADMIN | sudo | EXECUTE | ✅ ALLOWED | Admin full access |
| jane_operator | OPERATOR | SSH | EXECUTE | ❌ NOT ALLOWED | Default deny |
| jane_operator | OPERATOR | rm | EXECUTE | ❌ NOT ALLOWED | Default deny |
| jane_operator | OPERATOR | ls | EXECUTE | ❌ NOT ALLOWED | Default deny |
| nonexistent_user | - | SSH | EXECUTE | ❌ NOT ALLOWED | User not found |

## 🔄 Flow Diagrams

### Successful Access Flow

```
1. Client Request
   ↓
2. Controller: AccessControlController.checkSSHAccess()
   ↓
3. Service: DroolsAccessControlService.checkAccess()
   ↓
4. Repository: UserRepository.findByUsername()
   ↓
5. Database: SELECT * FROM users WHERE username = ?
   ↓
6. Repository: UserRoleRepository.findRoleIdsByUserId()
   ↓
7. Repository: RoleRepository.findAllById()
   ↓
8. Repository: ResourceRepository.findByResourceName()
   ↓
9. Repository: ActionRepository.findByActionName()
   ↓
10. Repository: PolicyRepository.findByRoleIdAndEnabledTrueOrderByPriorityDesc()
    ↓
11. Drools Engine: Evaluate rules
    ↓
12. Rule Matched: "Allow SSH for Developer"
    ↓
13. AccessDecision.setAllowed(true)
    ↓
14. Response: AccessResponse.allow()
    ↓
15. Client receives: {"allowed": true, "message": "ALLOWED"}
```

### Denied Access Flow

```
1. Client Request
   ↓
2. Controller → Service
   ↓
3. Load User, Role, Resource, Action, Policies
   ↓
4. Drools Engine: Evaluate rules
   ↓
5. Rule Matched: "Deny rm for Developer" (priority 20)
   ↓
6. AccessDecision.setAllowed(false)
   ↓
7. Response: AccessResponse.deny()
   ↓
8. Client receives: {"allowed": false, "message": "NOT ALLOWED"}
```

## 🧪 Testing Commands

### Using cURL

#### Test 1: Developer SSH Access
```bash
curl -X POST "http://localhost:8081/api/access/ssh?username=john_developer"
```

#### Test 2: Developer rm Command (Should Deny)
```bash
curl -X POST "http://localhost:8081/api/access/command?username=john_developer&command=rm"
```

#### Test 3: Developer ls Command (Should Allow)
```bash
curl -X POST "http://localhost:8081/api/access/command?username=john_developer&command=ls"
```

#### Test 4: Admin Full Access
```bash
curl -X POST "http://localhost:8081/api/access/command?username=admin_user&command=rm"
```

#### Test 5: Generic Check Endpoint
```bash
curl -X POST "http://localhost:8081/api/access/check" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_developer",
    "resourceName": "SSH",
    "actionName": "EXECUTE",
    "resourceType": "SERVICE"
  }'
```

### Using PowerShell

#### Test 1: Developer SSH Access
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/access/ssh?username=john_developer" -Method POST
```

#### Test 2: Generic Check
```powershell
$body = @{
    username = "john_developer"
    resourceName = "rm"
    actionName = "EXECUTE"
    resourceType = "COMMAND"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/access/check" -Method POST -Body $body -ContentType "application/json"
```

### Postman Collection

Create a Postman collection with these requests:

1. **Developer SSH Access**
   - Method: POST
   - URL: `http://localhost:8081/api/access/ssh?username=john_developer`

2. **Developer rm Command**
   - Method: POST
   - URL: `http://localhost:8081/api/access/command?username=john_developer&command=rm`

3. **Developer ls Command**
   - Method: POST
   - URL: `http://localhost:8081/api/access/command?username=john_developer&command=ls`

4. **Admin Full Access**
   - Method: POST
   - URL: `http://localhost:8081/api/access/command?username=admin_user&command=rm`

5. **Generic Check**
   - Method: POST
   - URL: `http://localhost:8081/api/access/check`
   - Body (JSON):
     ```json
     {
       "username": "john_developer",
       "resourceName": "SSH",
       "actionName": "EXECUTE",
       "resourceType": "SERVICE"
     }
     ```

## 🔍 Verification Steps

### 1. Verify Database Setup

```sql
-- Check users
SELECT * FROM users;

-- Check roles
SELECT * FROM roles;

-- Check user-role assignments
SELECT u.username, r.role_name 
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;

-- Check policies
SELECT p.policy_name, r.role_name, res.resource_name, a.action_name, p.effect, p.priority
FROM policies p
LEFT JOIN roles r ON p.role_id = r.id
LEFT JOIN resources res ON p.resource_id = res.id
LEFT JOIN actions a ON p.action_id = a.id
ORDER BY p.priority DESC;
```

### 2. Verify Application Logs

Check application logs for:
- Drools rule firing messages
- Database query execution
- Access decision results

### 3. Test All Scenarios

Run through all test scenarios and verify:
- ✅ Correct responses (allowed/not allowed)
- ✅ Correct messages
- ✅ All fields populated correctly
- ✅ No errors in logs

## 🐛 Troubleshooting

### Issue: User not found
**Solution:** Verify user exists in database:
```sql
SELECT * FROM users WHERE username = 'john_developer';
```

### Issue: Policy not matching
**Solution:** Check policies:
```sql
SELECT * FROM policies WHERE role_id = 2 AND enabled = true;
```

### Issue: Drools not firing rules
**Solution:** 
- Check `kmodule.xml` configuration
- Verify `access-control.drl` file location
- Check application logs for Drools errors

### Issue: Database connection error
**Solution:**
- Verify `.env` file has correct credentials
- Check Docker container is running: `docker ps`
- Test database connection manually

## 📝 Notes

1. **Default Deny**: If no policy matches, access is denied by default
2. **Policy Priority**: Higher priority policies are evaluated first
3. **DENY Override**: DENY policies (priority 20) override ALLOW policies (priority 10)
4. **Admin Override**: Admin role has full access regardless of policies
5. **Reactive**: All operations are non-blocking and reactive (Mono/Flux)

## 🎯 Summary

This ABAC system uses Drools to evaluate access control policies stored in the database. The system:

- ✅ Supports multiple roles and users
- ✅ Evaluates policies based on role, resource, and action
- ✅ Implements default deny (secure by default)
- ✅ Supports policy priorities
- ✅ Provides admin override capability
- ✅ Returns clear allow/deny responses

Test all scenarios to ensure the system works as expected!

