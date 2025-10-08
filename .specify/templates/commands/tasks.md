# Task Management Workflow Command: `/speckit.tasks`

**Constitutional Alignment**: This workflow ensures **Feature Independence & Testability** and **Structured Development Workflow** principles are maintained.

**Purpose**: Generate comprehensive task breakdown organized by user stories to enable independent implementation and testing of each feature slice.

**Input**: Implementation plan from `/specs/[FEATURE]/plan.md` and feature specification from `/specs/[FEATURE]/spec.md`

**Output**: Detailed task list organized by phases and user stories with clear dependencies and parallel execution opportunities.

## Command Usage

```bash
/speckit.tasks [FEATURE-NAME]
```

**Parameters**:
- `FEATURE-NAME`: Name of the feature for task generation (required)

## Workflow Execution

### Phase 0: Prerequisites Validation

**Constitutional Gate**: Must pass before task generation.

**Step 0.1: Input Validation**
- Verify `/specs/[FEATURE]/spec.md` exists and is complete
- Confirm `/specs/[FEATURE]/plan.md` exists and has technical context
- Validate user stories are prioritized (P1, P2, P3)
- Check success criteria are defined

**Step 0.2: Constitutional Compliance Check**
- [ ] Feature independence requirements met
- [ ] Template standardization followed
- [ ] Documentation-First approach applied
- [ ] Testability requirements documented

**Step 0.3: Template Preparation**
```bash
# Copy task template to feature directory
cp .specify/templates/tasks-template.md specs/[FEATURE]/tasks.md

# Verify template structure
grep -E "^## Phase" specs/[FEATURE]/tasks.md
```

### Phase 1: Task Structure Analysis

**Constitutional Alignment**: Feature Independence & Testability

**Step 1.1: User Story Analysis**
- Extract user stories from spec.md (US1, US2, US3, etc.)
- Identify priorities (P1 = MVP, P2 = Enhancement, P3 = Nice-to-have)
- Define independent test criteria for each story
- Document acceptance scenarios per story

**Step 1.2: Technical Dependency Mapping**
- Analyze plan.md for technical requirements
- Identify shared infrastructure needs
- Map testing framework requirements
- Document deployment considerations

**Step 1.3: Parallel Execution Planning**
- Identify tasks that can run in parallel [P]
- Map dependencies between user stories
- Plan resource allocation strategy
- Define checkpoint milestones

### Phase 2: Task Generation by Phase

**Constitutional Alignment**: Structured Development Workflow

**Step 2.1: Phase 1 Tasks (Setup)**
```bash
# Generate setup tasks based on plan.md technical context
cat >> specs/[FEATURE]/tasks.md << EOF

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create project structure per implementation plan
- [ ] T002 Initialize [language] project with [framework] dependencies
- [ ] T003 [P] Configure linting and formatting tools
EOF
```

**Step 2.2: Phase 2 Tasks (Foundational)**
```bash
# Generate foundational tasks (blocking prerequisites)
cat >> specs/[FEATURE]/tasks.md << EOF

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Setup database schema and migrations framework
- [ ] T005 [P] Implement authentication/authorization framework
- [ ] T006 [P] Setup API routing and middleware structure
- [ ] T007 Create base models/entities that all stories depend on

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel
EOF
```

**Step 2.3: User Story Tasks (Phase 3+)**
```bash
# Generate user story tasks for each prioritized story
for story in $(grep -E "User Story.*P[1-3]" specs/[FEATURE]/spec.md | head -3); do
    story_num=$(echo $story | grep -o "P[1-3]")
    cat >> specs/[FEATURE]/tasks.md << EOF

## Phase $(echo $story_num | tr -d 'P')$(($(echo $story_num | tr -d 'P') + 2)): User Story - [STORY TITLE] (Priority: $story_num) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story (OPTIONAL - only if tests requested) ⚠️

**NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T00$((story_num * 10 + 7)) [P] [US$story_num] Contract test for [endpoint] in tests/contract/test_[name].py
- [ ] T00$((story_num * 10 + 8)) [P] [US$story_num] Integration test for [user journey] in tests/integration/test_[name].py

### Implementation for User Story

- [ ] T00$((story_num * 10 + 9)) [P] [US$story_num] Create [Entity] model in src/models/[entity].py
- [ ] T00$((story_num * 10 + 10)) [US$story_num] Implement [Service] in src/services/[service].py
- [ ] T00$((story_num * 10 + 11)) [US$story_num] Implement [endpoint/feature] in src/[location]/[file].py

**Checkpoint**: At this point, User Story should be fully functional and testable independently
EOF
done
```

**Step 2.4: Polish Phase Tasks**
```bash
# Generate cross-cutting concern tasks
cat >> specs/[FEATURE]/tasks.md << EOF

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T0XX [P] Documentation updates in docs/
- [ ] T0XX Code cleanup and refactoring
- [ ] T0XX Performance optimization across all stories
- [ ] T0XX [P] Additional unit tests (if requested) in tests/unit/
- [ ] T0XX Security hardening
- [ ] T0XX Run quickstart.md validation
EOF
```

### Phase 3: Dependency Management

**Constitutional Alignment**: Feature Independence & Testability

**Step 3.1: Execution Order Definition**
```bash
# Document phase dependencies
cat >> specs/[FEATURE]/tasks.md << EOF

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority
EOF
```

**Step 3.2: Parallel Execution Guidelines**
```bash
# Document parallel execution opportunities
cat >> specs/[FEATURE]/tasks.md << EOF

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members
EOF
```

### Phase 4: Implementation Strategy

**Constitutional Alignment**: Documentation-First Approach

**Step 4.1: MVP Strategy Documentation**
```bash
# Document MVP-first approach
cat >> specs/[FEATURE]/tasks.md << EOF

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories
EOF
```

**Step 4.2: Team Strategy Guidelines**
```bash
# Document team collaboration approach
cat >> specs/[FEATURE]/tasks.md << EOF

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently
EOF
```

### Phase 5: Quality Assurance Integration

**Constitutional Alignment**: Template Standardization

**Step 5.1: Testing Strategy Documentation**
```bash
# Document testing approach for each user story
cat >> specs/[FEATURE]/tasks.md << EOF

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
EOF
```

**Step 5.2: Validation Checklist**
- [ ] All tasks have clear, actionable descriptions
- [ ] User stories are independently testable
- [ ] Parallel tasks properly marked with [P]
- [ ] Dependencies clearly documented
- [ ] Testing strategy integrated
- [ ] Constitutional compliance maintained

## Output Artifacts

The `/speckit.tasks` command generates:

1. **`tasks.md`** - Complete task breakdown with dependencies and execution strategy
2. **Task Numbering** - Sequential numbering (T001, T002, etc.) for tracking
3. **Phase Organization** - Clear separation of setup, foundational, and user story tasks
4. **Dependency Mapping** - Visual representation of task relationships
5. **Parallel Execution Plan** - Guidelines for concurrent development

## Error Handling

**Constitutional Violations**:
- Missing or incomplete specification
- Feature independence not maintained
- Template standardization not followed
- Documentation requirements not met

**Technical Issues**:
- Invalid feature directory structure
- Template parsing failures
- Task generation conflicts
- Dependency resolution errors

## Integration Points

**Constitutional Governance**:
- Task organization verifies feature independence
- User story separation ensures testability
- Template usage maintains standardization
- Documentation requirements enforced

**Development Workflow**:
- Tasks serve as input for implementation
- Checkpoints enable incremental delivery
- Parallel execution supports team scaling
- Quality gates ensure constitutional compliance

## Success Criteria

**Task Management Success**:
- [ ] All user stories have independent task breakdowns
- [ ] Clear dependencies and execution order defined
- [ ] Parallel execution opportunities identified
- [ ] Testing strategy integrated throughout
- [ ] Ready for implementation with clear next steps

**Constitutional Compliance**:
- [ ] Feature independence maintained in task organization
- [ ] Template standardization followed
- [ ] Documentation-First approach supported
- [ ] Structured workflow enabled
- [ ] Governance requirements satisfied

---

**Constitution Version**: 1.0.0 | **Template Version**: 1.0.0 | **Last Updated**: 2025-10-07