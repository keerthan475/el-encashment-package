# EL Encashment Database Design

## 1. Purpose

This database supports:

- Personnel master data
- Finance master data for personnel
- Monthly CGEIS salary history
- EL encashment preparation and settlement
- CGEIS bill preparation and settlement
- Bill, DV, and MRO lifecycle tracking
- Officer/staff category based reporting

The current application uses Oracle and Spring JPA. This design is written to fit Oracle first.

## 2. Business Modules

### 2.1 Personnel

Stores employee identity and classification.

### 2.2 Finance Data

Stores payroll-related reference data per employee.

### 2.3 Salary / CGEIS History

Stores employee-wise monthly CGEIS contribution history.

### 2.4 EL Encashment

Stores leave encashment claim records for:

- `Superannuation`
- `Retirement`
- `Home_Town`
- `All_India`

### 2.5 CGEIS Bills

Stores CGEIS settlement bills and bill line items.

### 2.6 Settlement Documents

Tracks:

- Bill number and bill date
- DV number, DV date, DV amount
- MRO number, MRO date, MRO amount

## 3. Recommended Logical Model

The best production design is to separate master tables, transaction headers, transaction line items, and payment/recovery documents.

### Core master tables

- `PERSONNEL`
- `FINANCE_DATA`
- `DISG_TYPE_MASTER`
- `PURPOSE_MASTER`
- `SEPARATION_REASON_MASTER`

### Transaction tables

- `EL_ENCASHMENT`
- `CGEIS_MONTHLY_CONTRIBUTION`
- `CGEIS_BILL`
- `CGEIS_BILL_ITEM`

### Document tables

- `BILL_REGISTER`
- `DV_REGISTER`
- `MRO_REGISTER`

### Link tables

- `EL_BILL_MAP`
- `EL_DV_MAP`
- `EL_MRO_MAP`
- `CGEIS_DV_MAP` if one DV can cover multiple CGEIS bills

## 4. Recommended Physical Schema

## 4.1 `DISG_TYPE_MASTER`

Classification of employee category.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `DISG_TYPE_ID` | `NUMBER(3)` | PK | 1=Officer, 2=Staff, 3=Staff |
| `DISG_TYPE_CODE` | `VARCHAR2(20)` | UNIQUE NOT NULL | Example: `OFFICER` |
| `DISG_TYPE_NAME` | `VARCHAR2(50)` | NOT NULL | Display label |
| `IS_ACTIVE` | `CHAR(1)` | NOT NULL | `Y` / `N` |

## 4.2 `PERSONNEL`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `PERSON_ID` | `NUMBER(19)` | PK | Surrogate key |
| `EMP_CODE` | `VARCHAR2(30)` | UNIQUE NOT NULL | Employee code |
| `DISG_TYPE_ID` | `NUMBER(3)` | FK NOT NULL | To `DISG_TYPE_MASTER` |
| `NAME` | `VARCHAR2(150)` | NOT NULL | Employee name |
| `DIVISION` | `VARCHAR2(100)` |  | Unit/division |
| `DOB` | `DATE` |  | Date of birth |
| `IS_ACTIVE` | `CHAR(1)` | NOT NULL | `Y` / `N` |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL | Audit |
| `UPDATED_AT` | `TIMESTAMP` | NOT NULL | Audit |

Indexes:

- `UK_PERSONNEL_EMP_CODE`
- `IDX_PERSONNEL_NAME`
- `IDX_PERSONNEL_DISG_TYPE`

## 4.3 `FINANCE_DATA`

One-to-one with personnel.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `FINANCE_ID` | `NUMBER(19)` | PK |  |
| `PERSON_ID` | `NUMBER(19)` | FK UNIQUE NOT NULL | To `PERSONNEL` |
| `BASIC_PAY` | `NUMBER(12,2)` |  |  |
| `SPECIAL_PAY` | `NUMBER(12,2)` |  |  |
| `GPF_ACCOUNT_NO` | `VARCHAR2(40)` |  |  |
| `PAN_NO` | `VARCHAR2(20)` |  | Useful for IT schedules |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL |  |
| `UPDATED_AT` | `TIMESTAMP` | NOT NULL |  |

Indexes:

- `UK_FINANCE_PERSON`
- `IDX_FINANCE_GPF`

## 4.4 `PURPOSE_MASTER`

Reference values for EL encashment purpose.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `PURPOSE_ID` | `NUMBER(5)` | PK |  |
| `PURPOSE_CODE` | `VARCHAR2(30)` | UNIQUE NOT NULL | `SUPERANNUATION`, `HOME_TOWN` |
| `PURPOSE_NAME` | `VARCHAR2(60)` | NOT NULL | Display label |
| `REQUIRES_BLOCK_PERIOD` | `CHAR(1)` | NOT NULL | `Y` / `N` |
| `ALLOWS_MRO` | `CHAR(1)` | NOT NULL | `Y` for LTC-style cases |

Suggested rows:

- `SUPERANNUATION`
- `RETIREMENT`
- `HOME_TOWN`
- `ALL_INDIA`

## 4.5 `EL_ENCASHMENT`

Main EL claim table. This should store the claim itself, not all downstream document values directly.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `EL_ENCASHMENT_ID` | `NUMBER(19)` | PK |  |
| `PERSON_ID` | `NUMBER(19)` | FK NOT NULL | To `PERSONNEL` |
| `PURPOSE_ID` | `NUMBER(5)` | FK NOT NULL | To `PURPOSE_MASTER` |
| `DO_PART_NUMBER` | `VARCHAR2(50)` | NOT NULL |  |
| `DO_PART_DATE` | `DATE` | NOT NULL |  |
| `EVENT_DATE` | `DATE` | NOT NULL | LTC/event/retirement date |
| `BLOCK_PERIOD` | `VARCHAR2(30)` |  | Required for LTC purposes |
| `EL_DAYS` | `NUMBER(5)` | NOT NULL |  |
| `HPL_DAYS` | `NUMBER(5)` | NOT NULL |  |
| `EL_AMOUNT` | `NUMBER(14,2)` | NOT NULL |  |
| `HPL_AMOUNT` | `NUMBER(14,2)` | NOT NULL |  |
| `TOTAL_AMOUNT` | `NUMBER(14,2)` | NOT NULL | Must equal EL + HPL |
| `IT_AMOUNT` | `NUMBER(14,2)` | DEFAULT 0 NOT NULL |  |
| `EDU_CESS` | `NUMBER(14,2)` | DEFAULT 0 NOT NULL |  |
| `IT_RECOVERY` | `NUMBER(14,2)` | DEFAULT 0 NOT NULL |  |
| `OTHER_RECOVERY` | `NUMBER(14,2)` | DEFAULT 0 NOT NULL |  |
| `OTHER_REMARK` | `VARCHAR2(250)` |  | Required if other recovery > 0 |
| `OTHER_TAXABLE` | `NUMBER(14,2)` | DEFAULT 0 NOT NULL |  |
| `OTHER_TAXABLE_REMARK` | `VARCHAR2(250)` |  | Required if other taxable > 0 |
| `GRAND_TOTAL` | `NUMBER(14,2)` | NOT NULL | Net payable |
| `STATUS` | `VARCHAR2(20)` | NOT NULL | `PREPARED`, `BILLED`, `DV_DONE`, `MRO_DONE`, `CLOSED` |
| `CREATED_DATE` | `DATE` | NOT NULL | Existing app behavior |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL | Audit |
| `UPDATED_AT` | `TIMESTAMP` | NOT NULL | Audit |

Checks:

- `EL_DAYS >= 0`
- `HPL_DAYS >= 0`
- `EL_DAYS + HPL_DAYS BETWEEN 1 AND 300`
- `EL_AMOUNT >= 0`
- `HPL_AMOUNT >= 0`
- `TOTAL_AMOUNT = EL_AMOUNT + HPL_AMOUNT`
- `IT_AMOUNT >= 0`
- `EDU_CESS >= 0`
- `IT_RECOVERY >= 0`
- `OTHER_RECOVERY >= 0`
- `OTHER_TAXABLE >= 0`

Indexes:

- `IDX_EL_PERSON`
- `IDX_EL_STATUS`
- `IDX_EL_CREATED_DATE`
- `IDX_EL_PURPOSE`
- `IDX_EL_DO_PART_DATE`

## 4.6 `BILL_REGISTER`

One bill can cover many EL claims. This is better than duplicating bill number into every EL row.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `BILL_ID` | `NUMBER(19)` | PK |  |
| `BILL_TYPE` | `VARCHAR2(20)` | NOT NULL | `EL`, `CGEIS` |
| `BILL_NO` | `VARCHAR2(10)` | NOT NULL | Existing rule is 3 digits |
| `BILL_DATE` | `DATE` | NOT NULL |  |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL |  |
| `UPDATED_AT` | `TIMESTAMP` | NOT NULL |  |

Unique key:

- `UK_BILL_TYPE_NO_DATE` on (`BILL_TYPE`, `BILL_NO`, `BILL_DATE`)

## 4.7 `EL_BILL_MAP`

Maps EL claims to a bill.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `EL_BILL_MAP_ID` | `NUMBER(19)` | PK |  |
| `EL_ENCASHMENT_ID` | `NUMBER(19)` | FK UNIQUE NOT NULL | One current bill per claim |
| `BILL_ID` | `NUMBER(19)` | FK NOT NULL |  |

Indexes:

- `UK_EL_BILL_ONE_TO_ONE` on `EL_ENCASHMENT_ID`
- `IDX_EL_BILL_BILL_ID`

## 4.8 `DV_REGISTER`

Stores disbursement voucher data.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `DV_ID` | `NUMBER(19)` | PK |  |
| `DV_TYPE` | `VARCHAR2(20)` | NOT NULL | `EL`, `CGEIS` |
| `DV_NO` | `VARCHAR2(10)` | NOT NULL | Existing rule is 4 digits |
| `DV_DATE` | `DATE` | NOT NULL |  |
| `DV_AMOUNT` | `NUMBER(14,2)` | NOT NULL |  |
| `DV_BALANCE` | `NUMBER(14,2)` | DEFAULT 0 | EL use case |
| `RECOVERY_CDA` | `NUMBER(14,2)` | DEFAULT 0 | EL use case |
| `CDA_REMARKS` | `VARCHAR2(250)` |  |  |
| `RECOVERY_CDA_TAX` | `NUMBER(14,2)` | DEFAULT 0 | EL use case |
| `CDA_TAX_REMARKS` | `VARCHAR2(250)` |  |  |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL |  |
| `UPDATED_AT` | `TIMESTAMP` | NOT NULL |  |

Unique key:

- `UK_DV_TYPE_NO_DATE` on (`DV_TYPE`, `DV_NO`, `DV_DATE`)

## 4.9 `EL_DV_MAP`

Maps EL claims to DV.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `EL_DV_MAP_ID` | `NUMBER(19)` | PK |  |
| `EL_ENCASHMENT_ID` | `NUMBER(19)` | FK UNIQUE NOT NULL |  |
| `DV_ID` | `NUMBER(19)` | FK NOT NULL |  |

## 4.10 `MRO_REGISTER`

Stores MRO details for EL LTC-style recovery cases.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `MRO_ID` | `NUMBER(19)` | PK |  |
| `MRO_NO` | `VARCHAR2(30)` | UNIQUE NOT NULL | Supports alphanumeric `/` `-` |
| `MRO_DATE` | `DATE` | NOT NULL |  |
| `MRO_AMOUNT` | `NUMBER(14,2)` | NOT NULL |  |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL |  |
| `UPDATED_AT` | `TIMESTAMP` | NOT NULL |  |

## 4.11 `EL_MRO_MAP`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `EL_MRO_MAP_ID` | `NUMBER(19)` | PK |  |
| `EL_ENCASHMENT_ID` | `NUMBER(19)` | FK UNIQUE NOT NULL | One MRO per claim in current flow |
| `MRO_ID` | `NUMBER(19)` | FK NOT NULL |  |

## 4.12 `SEPARATION_REASON_MASTER`

Reference table for CGEIS reason.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `REASON_ID` | `NUMBER(5)` | PK |  |
| `REASON_CODE` | `VARCHAR2(30)` | UNIQUE NOT NULL | `SUPERANNUATION`, `EXPIRED`, `RESIGNATION`, `VRS` |
| `REASON_NAME` | `VARCHAR2(60)` | NOT NULL |  |

## 4.13 `CGEIS_MONTHLY_CONTRIBUTION`

This is the normalized replacement for the current `SALARY` table.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `CONTRIBUTION_ID` | `NUMBER(19)` | PK |  |
| `PERSON_ID` | `NUMBER(19)` | FK NOT NULL |  |
| `MONTH_YEAR` | `DATE` | NOT NULL | Always first day of month |
| `CGEIS` | `NUMBER(12,2)` | NOT NULL | Monthly contribution |
| `VALUE_AMOUNT` | `NUMBER(12,2)` |  | Saved value multiplier |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL |  |
| `UPDATED_AT` | `TIMESTAMP` | NOT NULL |  |

Unique key:

- `UK_CGEIS_PERSON_MONTH` on (`PERSON_ID`, `MONTH_YEAR`)

Indexes:

- `IDX_CGEIS_PERSON`
- `IDX_CGEIS_MONTH`

## 4.14 `CGEIS_BILL`

Header table for CGEIS claim.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `CGEIS_BILL_ID` | `NUMBER(19)` | PK |  |
| `PERSON_ID` | `NUMBER(19)` | FK NOT NULL |  |
| `REASON_ID` | `NUMBER(5)` | FK NOT NULL | To `SEPARATION_REASON_MASTER` |
| `DO_PART_NUMBER` | `VARCHAR2(50)` | NOT NULL |  |
| `DO_PART_DATE` | `DATE` | NOT NULL |  |
| `BILL_ID` | `NUMBER(19)` | FK NOT NULL | Link to `BILL_REGISTER` |
| `TOTAL_AMOUNT` | `NUMBER(14,2)` | NOT NULL |  |
| `IT_AMOUNT` | `NUMBER(14,2)` | DEFAULT 0 NOT NULL | Current app stores zero |
| `EDU_CESS` | `NUMBER(14,2)` | DEFAULT 0 NOT NULL | Current app stores zero |
| `TOTAL_IT` | `NUMBER(14,2)` | DEFAULT 0 NOT NULL | Current app stores zero |
| `NET_PAY` | `NUMBER(14,2)` | NOT NULL |  |
| `INSURANCE_COVERAGE` | `NUMBER(14,2)` | DEFAULT 0 NOT NULL |  |
| `OTHER_RECOVERY` | `NUMBER(14,2)` | DEFAULT 0 NOT NULL |  |
| `REMARKS` | `VARCHAR2(250)` |  | Required if other recovery > 0 |
| `STATUS` | `VARCHAR2(20)` | NOT NULL | `BILLED`, `DV_DONE`, `CLOSED` |
| `CREATED_DATE` | `DATE` | NOT NULL | Existing behavior |
| `CREATED_AT` | `TIMESTAMP` | NOT NULL |  |
| `UPDATED_AT` | `TIMESTAMP` | NOT NULL |  |

Indexes:

- `IDX_CGEIS_BILL_PERSON`
- `IDX_CGEIS_BILL_STATUS`
- `IDX_CGEIS_BILL_CREATED_DATE`

## 4.15 `CGEIS_BILL_ITEM`

Stores grouped month ranges used in a CGEIS bill.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `CGEIS_BILL_ITEM_ID` | `NUMBER(19)` | PK |  |
| `CGEIS_BILL_ID` | `NUMBER(19)` | FK NOT NULL | To `CGEIS_BILL` |
| `FROM_MONTH` | `DATE` | NOT NULL | First day of month |
| `TO_MONTH` | `DATE` | NOT NULL | First day of month |
| `CGEIS` | `NUMBER(12,2)` | NOT NULL |  |
| `VALUE_AMOUNT` | `NUMBER(12,2)` | NOT NULL | Current item value |
| `LEGACY_VALUE` | `NUMBER(12,2)` |  | For compatibility if needed |
| `TIMES` | `NUMBER(5)` | NOT NULL | Month count / multiplier |
| `LINE_AMOUNT` | `NUMBER(14,2)` | NOT NULL | `VALUE_AMOUNT * TIMES` |

Checks:

- `TO_MONTH >= FROM_MONTH`
- `TIMES > 0`
- `LINE_AMOUNT >= 0`

Indexes:

- `IDX_CGEIS_ITEM_BILL`
- `IDX_CGEIS_ITEM_RANGE`

## 4.16 `CGEIS_DV_MAP`

If each CGEIS bill has one DV, keep a unique map. If a DV can cover many bills, keep this as many-to-one.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `CGEIS_DV_MAP_ID` | `NUMBER(19)` | PK |  |
| `CGEIS_BILL_ID` | `NUMBER(19)` | FK UNIQUE NOT NULL | Current app behavior |
| `DV_ID` | `NUMBER(19)` | FK NOT NULL | `DV_REGISTER` row with `DV_TYPE='CGEIS'` |

## 5. Current Application Mapping

The current code maps almost directly to these existing tables:

- `PERSONNEL`
- `FINANCE_DATA`
- `SALARY`
- `EL_ENCASHMENT`
- `CGEIS_BILL`
- `CGEIS_BILL_ITEM`

### Current design issue

`EL_ENCASHMENT` currently stores:

- claim data
- bill data
- DV data
- difference DV placeholders
- MRO data

This works for a prototype, but it is denormalized and will become difficult to maintain when:

- one bill contains many claims
- one DV covers many records
- document history needs audit
- corrections or reprocessing are required

## 6. Recommended Status Flow

### EL Encashment

`PREPARED` -> `BILLED` -> `DV_DONE` -> `MRO_DONE` -> `CLOSED`

Notes:

- `MRO_DONE` applies only to `HOME_TOWN` and `ALL_INDIA`
- `CLOSED` can be set once no pending finance action remains

### CGEIS

`BILLED` -> `DV_DONE` -> `CLOSED`

## 7. Validation Rules

## 7.1 EL Encashment

- `DO_PART_DATE <= EVENT_DATE`
- For `HOME_TOWN` / `ALL_INDIA`:
  - `BLOCK_PERIOD` required
  - `EL_DAYS BETWEEN 1 AND 10`
  - `HPL_DAYS = 0`
- For other purposes:
  - `EL_DAYS BETWEEN 0 AND 300`
  - `HPL_DAYS BETWEEN 0 AND 300`
  - `EL_DAYS + HPL_DAYS BETWEEN 1 AND 300`
- `TOTAL_AMOUNT = EL_AMOUNT + HPL_AMOUNT`
- `GRAND_TOTAL = TOTAL_AMOUNT - (IT_RECOVERY + OTHER_RECOVERY + OTHER_TAXABLE)`
- If `OTHER_RECOVERY > 0`, `OTHER_REMARK` required
- If `OTHER_TAXABLE > 0`, `OTHER_TAXABLE_REMARK` required

## 7.2 Bill

- `BILL_NO` must be unique by bill type and period rule adopted by finance
- `BILL_DATE >= CREATED_DATE` for EL record assignment

## 7.3 DV

- `DV_DATE >= BILL_DATE`
- `DV_AMOUNT > 0`
- If `RECOVERY_CDA > 0`, `CDA_REMARKS` required
- If `RECOVERY_CDA_TAX > 0`, `CDA_TAX_REMARKS` required

## 7.4 MRO

- Allowed only for LTC-style EL purposes
- `MRO_DATE >= DV_DATE`
- `MRO_AMOUNT > 0`
- For selected records, total MRO amount should equal sum of selected recoverable DV amounts

## 7.5 CGEIS Monthly Contribution

- `MONTH_YEAR` stored as first day of month
- No duplicate row for same employee + month
- Ranges inserted must not overlap existing months

## 8. Indexing Strategy

Use these indexes because current screens query by these fields:

- Personnel name search
- Employee code lookup
- Disg type filter
- EL pending bill: `STATUS`, `PERSON_ID`, `CREATED_DATE`
- EL billed records: `STATUS`, linked bill date
- EL pending DV: `STATUS`, bill mapping
- EL pending MRO: `STATUS`, `PURPOSE_ID`
- CGEIS by employee and month
- CGEIS bill history by employee
- CGEIS pending DV by status

Recommended high-value indexes:

- `PERSONNEL(EMP_CODE)`
- `PERSONNEL(DISG_TYPE_ID, NAME)`
- `CGEIS_MONTHLY_CONTRIBUTION(PERSON_ID, MONTH_YEAR)`
- `EL_ENCASHMENT(PERSON_ID, STATUS, CREATED_DATE)`
- `EL_ENCASHMENT(PURPOSE_ID, STATUS)`
- `CGEIS_BILL(PERSON_ID, STATUS, CREATED_DATE)`
- `BILL_REGISTER(BILL_TYPE, BILL_NO, BILL_DATE)`
- `DV_REGISTER(DV_TYPE, DV_NO, DV_DATE)`

## 9. Sequence Design for Oracle

Recommended sequences:

- `SEQ_PERSONNEL`
- `SEQ_FINANCE_DATA`
- `SEQ_EL_ENCASHMENT`
- `SEQ_BILL_REGISTER`
- `SEQ_DV_REGISTER`
- `SEQ_MRO_REGISTER`
- `SEQ_CGEIS_MONTHLY_CONTRIBUTION`
- `SEQ_CGEIS_BILL`
- `SEQ_CGEIS_BILL_ITEM`

## 10. Audit Columns

Add these to all transactional tables:

- `CREATED_AT TIMESTAMP`
- `CREATED_BY VARCHAR2(50)`
- `UPDATED_AT TIMESTAMP`
- `UPDATED_BY VARCHAR2(50)`

Optional:

- `ROW_VERSION NUMBER(10)` for optimistic locking

## 11. Reporting View Suggestions

Create views for common screens:

### `VW_EL_PENDING_BILLS`

EL claims with no bill assigned.

### `VW_EL_PENDING_DV`

EL claims billed but DV not assigned.

### `VW_EL_PENDING_MRO`

EL claims with LTC purpose, DV assigned, MRO pending.

### `VW_CGEIS_PENDING_DV`

CGEIS bills without DV.

### `VW_PERSONNEL_FINANCE`

Personnel joined with finance data for reports and search.

## 12. DDL Starter Example

```sql
CREATE TABLE PERSONNEL (
    PERSON_ID NUMBER(19) PRIMARY KEY,
    EMP_CODE VARCHAR2(30) NOT NULL UNIQUE,
    DISG_TYPE_ID NUMBER(3) NOT NULL,
    NAME VARCHAR2(150) NOT NULL,
    DIVISION VARCHAR2(100),
    DOB DATE,
    IS_ACTIVE CHAR(1) DEFAULT 'Y' NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE TABLE FINANCE_DATA (
    FINANCE_ID NUMBER(19) PRIMARY KEY,
    PERSON_ID NUMBER(19) NOT NULL UNIQUE,
    BASIC_PAY NUMBER(12,2),
    SPECIAL_PAY NUMBER(12,2),
    GPF_ACCOUNT_NO VARCHAR2(40),
    PAN_NO VARCHAR2(20),
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT FK_FINANCE_PERSON
        FOREIGN KEY (PERSON_ID) REFERENCES PERSONNEL(PERSON_ID)
);

CREATE TABLE CGEIS_MONTHLY_CONTRIBUTION (
    CONTRIBUTION_ID NUMBER(19) PRIMARY KEY,
    PERSON_ID NUMBER(19) NOT NULL,
    MONTH_YEAR DATE NOT NULL,
    CGEIS NUMBER(12,2) NOT NULL,
    VALUE_AMOUNT NUMBER(12,2),
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT FK_CGEIS_CONTRIB_PERSON
        FOREIGN KEY (PERSON_ID) REFERENCES PERSONNEL(PERSON_ID),
    CONSTRAINT UK_CGEIS_PERSON_MONTH UNIQUE (PERSON_ID, MONTH_YEAR)
);
```

## 13. Best Recommendation For This Project

If you want the safest path for the current app:

1. Keep the current six tables working.
2. Add status columns and stronger unique/index constraints.
3. In the next step, extract bill, DV, and MRO details out of `EL_ENCASHMENT`.
4. Introduce `BILL_REGISTER`, `DV_REGISTER`, and `MRO_REGISTER`.
5. Migrate historical rows into mapping tables.

## 14. Minimum Tables Needed Right Now

If you want only the practical minimum for this app, these are enough:

- `PERSONNEL`
- `FINANCE_DATA`
- `SALARY` or `CGEIS_MONTHLY_CONTRIBUTION`
- `EL_ENCASHMENT`
- `CGEIS_BILL`
- `CGEIS_BILL_ITEM`

But for a complete and scalable database design, the normalized model above is the better choice.
