# Smart Farm — Baleegh Master Specification

## Product Definition

Smart Farm is a farm operating system. Baleegh is its conversational operating brain. Voice is the primary interaction channel. The existing Room farm database remains the system of record. Dashboards, tables, charts and forms are supporting interfaces.

Core loop:

Owner speaks -> speech recognition -> Baleegh understanding -> Farm Memory + current Farm State -> decision/planning -> confirmation when consequential -> action execution -> verification -> spoken response.

## Non-Negotiable Product Requirements

1. The owner must be able to operate the farm primarily by speaking.
2. Baleegh must understand Egyptian Arabic naturally enough for farm operations.
3. Baleegh must know the farm's persistent scope and operating preferences.
4. Baleegh must inspect real database state before answering state-dependent questions.
5. Baleegh must resolve people, fields, crops, inventory, equipment and other entities from actual farm records.
6. Baleegh must support multi-step requests and cross-domain reasoning.
7. Read/analyze actions may execute automatically.
8. Important mutations, financial actions, destructive actions and externally consequential actions require explicit confirmation unless a future permission policy explicitly authorizes them.
9. Baleegh must never invent farm facts, completed actions, purchases, balances or commitments.
10. Every executed action must be verifiable and the user must receive a clear result.
11. Farm onboarding must be conversational and persistent. Baleegh should learn the farm rather than repeatedly asking the same setup questions.
12. The app must remain usable when the network is unavailable for local data operations.
13. API keys must never be hardcoded or committed.
14. Existing working farm entities and repository operations must be reused rather than duplicated.
15. Each existing source file is modified at most once. All known changes for a file must be consolidated into one replacement.

## Baleegh Layers

### 1. Farm Memory
Persistent semantic knowledge that is not merely a transaction:
- owner identity
- farm identity and location
- operating practices
- preferences
- recurring rules
- supplier/customer relationships
- aliases and conversational names
- learned terminology
- onboarding progress

Existing AhmedPreference may be used as a seed, but memory access should be abstracted so the conversational layer is not coupled directly to a UI preference table.

### 2. Farm State
A targeted read model over Room:
- current inventory and low stock
- lands and crops
- workers and today's attendance
- active tasks
- equipment and maintenance
- water activity
- purchases, sales, treasury and debts
- livestock and production

The state layer must query only the domains needed for the current request.

### 3. Conversation Understanding
Produces structured analysis:
- intent
- entities
- resolved entity IDs/codes
- quantities
- amounts
- dates/deadlines
- commitments
- observations/problems
- confidence
- missing information
- proposed actions
- confirmation requirement

### 4. Decision / Planning
Turns understanding plus farm state into a safe plan.
A plan can contain multiple ordered actions and read steps. Planning must not mutate the database.

### 5. Action Engine
Executes approved actions through existing repository/DAO business operations. It must be idempotency-aware where practical and return structured execution results.

### 6. Verification
After mutation, reload the affected state and verify the intended change. If verification fails, report failure rather than claiming success.

### 7. Voice
Speech input/output is an adapter around the Baleegh engine. The engine must not depend on a specific speech SDK. UI should support hands-free turn-taking when the platform permits it.

## Safety Policy

Automatic:
- read inventory/status
- read workers/tasks/finance
- summarize
- analyze
- identify low stock
- identify overdue/upcoming work
- answer farm-state questions

Confirmation by default:
- create/change/delete important records
- purchases
- sales
- treasury movements
- debt changes
- inventory consumption/additions when not clearly stated as an explicit record command
- worker attendance changes
- equipment status changes
- destructive operations
- external communications

The confirmation must state exactly what will happen, with affected entity, quantity/amount/date and relevant source when known.

## Conversational Onboarding

Baleegh starts with a short introduction and discovers the farm incrementally:
- owner
- farm name/location
- lands/areas
- crops and seasons
- livestock
- workers
- equipment
- inventory
- suppliers/customers
- finance practices
- irrigation/water
- recurring operations
- priorities/preferences

It should save durable facts as they become sufficiently certain, avoid duplicate questions, and allow the owner to say “skip”, “later”, or correct information.

## Canonical Examples

“بليغ، شوف المخزون وقولي إيه اللي قرب يخلص.”
-> inspect current inventory and thresholds -> answer.

“أحمد قال إنه هيجيب الطلمبة بكرة.”
-> resolve Ahmed -> identify pump if possible -> interpret commitment -> propose task/reminder -> confirm if needed.

“سجل إننا خلصنا ري الأرض الغربية.”
-> resolve land -> create/complete the appropriate operation record -> verify -> report.

“هل السماد اللي عندنا يكفي العمليات الأسبوع الجاي؟”
-> combine inventory + crops/operations/tasks + dates -> calculate/estimate only from known data -> clearly distinguish recorded facts from estimates.

“الطلمبة القديمة عملت مشكلة تاني.”
-> resolve equipment from memory/current records -> inspect maintenance history -> summarize prior incidents -> propose next action without inventing a diagnosis.

## Implementation Strategy

Do not restart the repository.

First stabilize and prove the current Android build. Then implement the Baleegh core in small, testable layers:
1. baseline build/integrity
2. domain-safe Baleegh models
3. targeted Farm State provider
4. persistent Farm Memory service
5. structured planner
6. action firewall and execution/verification
7. conversational onboarding
8. real speech input/output adapters
9. hands-free conversation loop
10. cross-domain reasoning
11. anomaly/risk layer
12. visual UI refinement

The web preview should eventually visualize the same operating model, not become a separate fake backend.

## Acceptance Standard

A release is not considered successful merely because a chat screen works. Baleegh must demonstrate:
- understands natural Arabic requests
- retrieves real farm state
- resolves actual entities
- plans multi-step work
- protects consequential actions
- executes through real repository operations
- verifies changes
- remembers durable farm facts
- can conduct onboarding
- speaks results back to the owner
- does not fabricate state or actions
