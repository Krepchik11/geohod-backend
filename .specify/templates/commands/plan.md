# Planning Workflow Command: `/speckit.plan`

**Constitutional Alignment**: This workflow ensures **Structured Development Workflow** and **Documentation-First Approach** principles are followed.

**Purpose**: Execute comprehensive feature planning process that creates implementation roadmap aligned with constitutional governance.

**Input**: Feature specification from `/specs/[FEATURE]/spec.md`

**Output**: Complete implementation plan with technical context, project structure, and constitutional compliance verification.

## Command Usage

```bash
/speckit.plan [FEATURE-NAME] [ARGUMENTS]
```

**Parameters**:
- `FEATURE-NAME`: Name of the feature to plan (required)
- `ARGUMENTS`: User description for feature context (optional)

## Workflow Execution

### Phase 0: Initialization & Validation

**Constitutional Gate**: Must pass before proceeding.

**Step 0.1: Constitutional Compliance Check**
- Verify constitution file exists and is current
- Confirm template standardization compliance
- Validate feature independence requirements

**Step 0.2: Feature Specification Validation**
- Confirm spec.md exists in `/specs/[FEATURE]/`
- Verify user stories are defined with priorities (P1, P2, P3)
- Validate success criteria are measurable
- Check functional requirements are documented

**Step 0.3: Directory Structure Creation**
```bash
# Create feature planning directory
mkdir -p specs/[FEATURE]/
mkdir -p specs/[FEATURE]/contracts/

# Copy planning template
cp .specify/templates/plan-template.md specs/[FEATURE]/plan.md
```

### Phase 1: Technical Research & Analysis

**Constitutional Alignment**: Documentation-First Approach

**Step 1.1: Technical Context Analysis**
- Analyze project requirements for technical stack
- Research optimal technology choices
- Document constraints and performance requirements
- Identify integration points and dependencies

**Step 1.2: Architecture Decision**
- Determine project structure (single/web/mobile)
- Define source code organization
- Plan testing strategy
- Document storage and deployment approach

**Step 1.3: Research Documentation**
```bash
# Create research documentation
cat > specs/[FEATURE]/research.md << 'EOF'
# Technical Research: [FEATURE]

## Technology Analysis
[Document technical research findings]

## Architecture Options
[Compare different approaches]

## Recommendation
[Chosen approach with rationale]
EOF
```

### Phase 2: Implementation Design

**Constitutional Alignment**: Feature Independence & Testability

**Step 2.1: Data Model Design**
- Design entities and relationships
- Document data contracts
- Define validation requirements
- Plan migration strategy

**Step 2.2: API Contract Design**
```bash
# Create API contracts directory
mkdir -p specs/[FEATURE]/contracts/

# Generate contract files based on user stories
for story in US1 US2 US3; do
    cat > specs/[FEATURE]/contracts/${story}.json << EOF
{
  "user_story": "$story",
  "endpoints": [
    {
      "path": "/api/[endpoint]",
      "method": "POST",
      "request": {},
      "response": {}
    }
  ],
  "independence": "Can be tested independently"
}
EOF
done
```

**Step 2.3: Project Structure Definition**
- Define complete directory structure
- Document file organization
- Plan testing structure
- Define deployment units

### Phase 3: Planning Documentation

**Constitutional Alignment**: Template Standardization

**Step 3.1: Plan Template Completion**
- Fill technical context section
- Document project structure decision
- Complete complexity tracking
- Add constitutional compliance notes

**Step 3.2: Quickstart Documentation**
```bash
# Generate feature-specific quickstart
cat > specs/[FEATURE]/quickstart.md << 'EOF'
# [FEATURE] Quick Start

## Setup
[Feature-specific setup instructions]

## Testing
[How to test this feature independently]

## Usage
[Feature usage examples]
EOF
```

**Step 3.3: Data Model Documentation**
```bash
# Create data model documentation
cat > specs/[FEATURE]/data-model.md << 'EOF'
# Data Model: [FEATURE]

## Entities
[Entity definitions and relationships]

## Validation Rules
[Data validation requirements]

## Migration Strategy
[Database migration approach]
EOF
```

### Phase 4: Constitutional Compliance Verification

**Constitutional Gate**: Must pass before implementation begins.

**Step 4.1: Principle Alignment Check**
- [ ] Structured Development Workflow: Process followed correctly
- [ ] Constitution-Driven Governance: All decisions documented
- [ ] Template Standardization: Approved templates used
- [ ] Feature Independence: Each story independently testable
- [ ] Documentation-First: All specs complete before planning

**Step 4.2: Quality Gates**
- [ ] Technical context complete and realistic
- [ ] Project structure decision documented
- [ ] Success criteria measurable
- [ ] Independence requirements met
- [ ] Template consistency maintained

**Step 4.3: Approval Process**
- [ ] Technical review completed
- [ ] Constitutional compliance verified
- [ ] Stakeholder approval obtained
- [ ] Version control committed

## Output Artifacts

The `/speckit.plan` command generates:

1. **`plan.md`** - Complete implementation plan with technical context
2. **`research.md`** - Technical research and analysis findings
3. **`data-model.md`** - Data model design and validation rules
4. **`quickstart.md`** - Feature-specific setup and usage guide
5. **`contracts/`** - API contracts for all user stories
6. **`tasks.md`** - Task breakdown (generated by separate `/speckit.tasks` command)

## Error Handling

**Constitutional Violations**:
- Missing constitution file
- Template non-compliance
- Incomplete specifications
- Feature independence violations

**Technical Issues**:
- Invalid feature name format
- Directory creation failures
- Template access errors
- Research analysis failures

## Integration Points

**Constitutional Governance**:
- All planning decisions logged for governance review
- Template usage tracked for standardization compliance
- Constitutional principles verified at each gate

**Development Workflow**:
- Planning output serves as input for `/speckit.tasks`
- Implementation must follow documented project structure
- Testing strategy integrated with overall project approach

## Success Criteria

**Planning Success**:
- [ ] Complete technical context documented
- [ ] Project structure clearly defined
- [ ] All user stories have implementation path
- [ ] Constitutional compliance verified
- [ ] Ready for task breakdown and implementation

**Constitutional Compliance**:
- [ ] All five principles followed throughout process
- [ ] Template standardization maintained
- [ ] Feature independence preserved
- [ ] Documentation completed before implementation
- [ ] Governance requirements satisfied

---

**Constitution Version**: 1.0.0 | **Template Version**: 1.0.0 | **Last Updated**: 2025-10-07