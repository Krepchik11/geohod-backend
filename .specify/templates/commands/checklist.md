# Checklist Workflow Command: `/speckit.checklist`

**Constitutional Alignment**: This workflow ensures **Template Standardization** and **Constitution-Driven Governance** principles are maintained.

**Purpose**: Generate verification checklists based on feature context, requirements, and constitutional compliance needs to ensure quality and governance standards.

**Input**: Feature specification from `/specs/[FEATURE]/spec.md`, implementation plan from `/specs/[FEATURE]/plan.md`, and task list from `/specs/[FEATURE]/tasks.md`

**Output**: Comprehensive verification checklist organized by categories with specific, actionable items aligned with constitutional governance.

## Command Usage

```bash
/speckit.checklist [FEATURE-NAME] [CHECKLIST-TYPE]
```

**Parameters**:
- `FEATURE-NAME`: Name of the feature for checklist generation (required)
- `CHECKLIST-TYPE`: Type of checklist (optional: "quality", "compliance", "security", "performance", "custom")

## Workflow Execution

### Phase 0: Prerequisites & Constitutional Compliance

**Constitutional Gate**: Must pass before checklist generation.

**Step 0.1: Input Validation**
- Verify `/specs/[FEATURE]/spec.md` exists and is complete
- Confirm `/specs/[FEATURE]/plan.md` exists with technical context
- Validate `/specs/[FEATURE]/tasks.md` exists with task breakdown
- Check constitutional compliance of all input documents

**Step 0.2: Constitutional Compliance Check**
- [ ] All input documents use approved templates
- [ ] Feature independence requirements maintained
- [ ] Documentation-First approach followed
- [ ] Template standardization applied consistently

**Step 0.3: Template Preparation**
```bash
# Copy checklist template to feature directory
cp .specify/templates/checklist-template.md specs/[FEATURE]/checklist.md

# Determine checklist type based on feature context
if [ -z "$CHECKLIST_TYPE" ]; then
    CHECKLIST_TYPE="comprehensive"
fi

# Initialize checklist metadata
cat > specs/[FEATURE]/.checklist-metadata << EOF
Feature: [FEATURE]
Type: $CHECKLIST_TYPE
Created: $(date)
Constitution Version: 1.0.0
Status: Generated
EOF
```

### Phase 1: Checklist Structure Analysis

**Constitutional Alignment**: Template Standardization

**Step 1.1: Feature Context Analysis**
- Extract user stories and priorities from spec.md
- Identify technical stack from plan.md
- Map implementation phases from tasks.md
- Determine risk areas and complexity factors

**Step 1.2: Category Definition**
- Define verification categories based on feature type
- Map constitutional principles to verification areas
- Identify quality gates and compliance checkpoints
- Plan evidence collection requirements

**Step 1.3: Item Generation Strategy**
- Create specific, actionable checklist items
- Ensure each item has clear pass/fail criteria
- Map items to constitutional principles
- Define evidence and documentation requirements

### Phase 2: Category-Based Checklist Generation

**Constitutional Alignment**: Constitution-Driven Governance

**Step 2.1: Constitutional Compliance Category**
```bash
# Generate constitutional compliance verification items
cat >> specs/[FEATURE]/checklist.md << EOF

## Constitutional Compliance

- [ ] CHK001 Structured Development Workflow: All development follows speckit process with proper phase gates
- [ ] CHK002 Constitution-Driven Governance: All decisions documented and aligned with constitutional principles
- [ ] CHK003 Template Standardization: Only approved templates from .specify/templates/ used throughout
- [ ] CHK004 Feature Independence: Each user story is independently testable and deliverable
- [ ] CHK005 Documentation-First: All specifications completed before implementation begins
- [ ] CHK006 Version Control: Constitutional compliance tracked and versioned appropriately
- [ ] CHK007 Amendment Process: Any constitutional deviations documented with rationale and approval
EOF
```

**Step 2.2: Feature-Specific Quality Category**
```bash
# Generate feature-specific quality verification items
cat >> specs/[FEATURE]/checklist.md << EOF

## Feature Quality & Functionality

- [ ] CHK008 User Story Independence: Each story delivers standalone value and can be tested independently
- [ ] CHK009 Acceptance Criteria: All user stories have clear, testable acceptance scenarios
- [ ] CHK010 Success Criteria: Measurable outcomes defined and achievable (SC-001, SC-002, etc.)
- [ ] CHK011 Edge Cases: Error scenarios and boundary conditions properly handled
- [ ] CHK012 Integration Points: Feature integrates correctly with existing system components
- [ ] CHK013 Performance Requirements: Feature meets documented performance goals and constraints
EOF
```

**Step 2.3: Technical Implementation Category**
```bash
# Generate technical verification items based on plan.md
cat >> specs/[FEATURE]/checklist.md << EOF

## Technical Implementation

- [ ] CHK014 Project Structure: Implementation follows documented project structure from plan.md
- [ ] CHK015 Dependencies: All required dependencies properly configured and documented
- [ ] CHK016 Testing Framework: Testing strategy implemented according to technical context
- [ ] CHK017 Code Quality: Code follows project standards for [language] and [framework]
- [ ] CHK018 Security: Security considerations addressed based on feature requirements
- [ ] CHK019 Performance: Implementation meets performance goals from technical context
- [ ] CHK020 Scalability: Solution designed to handle documented scale requirements
EOF
```

**Step 2.4: Documentation and Governance Category**
```bash
# Generate documentation and governance verification items
cat >> specs/[FEATURE]/checklist.md << EOF

## Documentation & Governance

- [ ] CHK021 Specification Accuracy: Implementation matches feature specification requirements
- [ ] CHK022 Technical Documentation: All technical decisions documented in plan.md
- [ ] CHK023 API Documentation: All endpoints and contracts properly documented
- [ ] CHK024 User Documentation: Feature usage documented in quickstart.md
- [ ] CHK025 Governance Compliance: All constitutional governance requirements satisfied
- [ ] CHK026 Template Consistency: All artifacts use approved templates consistently
- [ ] CHK027 Version Tracking: All changes tracked with proper version control
EOF
```

### Phase 3: Custom Checklist Types

**Constitutional Alignment**: Template Standardization

**Step 3.1: Quality-Focused Checklist**
```bash
if [ "$CHECKLIST_TYPE" = "quality" ]; then
    cat >> specs/[FEATURE]/checklist.md << EOF

## Quality Assurance

- [ ] CHK100 Code Review: All code passes peer review with quality standards
- [ ] CHK101 Unit Testing: Comprehensive unit tests cover all major functionality
- [ ] CHK102 Integration Testing: Feature integrates correctly with existing components
- [ ] CHK103 Performance Testing: Load and performance tests pass requirements
- [ ] CHK104 Accessibility: Feature meets accessibility standards (if applicable)
- [ ] CHK105 User Experience: Feature provides intuitive and responsive user experience
EOF
fi
```

**Step 3.2: Security-Focused Checklist**
```bash
if [ "$CHECKLIST_TYPE" = "security" ]; then
    cat >> specs/[FEATURE]/checklist.md << EOF

## Security Verification

- [ ] CHK200 Authentication: Proper authentication mechanisms implemented
- [ ] CHK201 Authorization: Access controls properly configured and tested
- [ ] CHK202 Data Protection: Sensitive data encrypted in transit and at rest
- [ ] CHK203 Input Validation: All inputs validated and sanitized
- [ ] CHK204 Security Headers: Appropriate security headers configured
- [ ] CHK205 Dependency Security: No known vulnerabilities in dependencies
EOF
fi
```

**Step 3.3: Performance-Focused Checklist**
```bash
if [ "$CHECKLIST_TYPE" = "performance" ]; then
    cat >> specs/[FEATURE]/checklist.md << EOF

## Performance Verification

- [ ] CHK300 Response Times: Feature meets documented response time requirements
- [ ] CHK301 Throughput: System handles expected concurrent user load
- [ ] CHK302 Resource Usage: Memory and CPU usage within acceptable limits
- [ ] CHK303 Database Performance: Database queries optimized for performance
- [ ] CHK304 Caching Strategy: Appropriate caching implemented where beneficial
- [ ] CHK305 Scalability: Solution scales according to documented requirements
EOF
fi
```

### Phase 4: Evidence and Validation Requirements

**Constitutional Alignment**: Constitution-Driven Governance

**Step 4.1: Evidence Collection Guidelines**
```bash
# Document evidence requirements for checklist verification
cat >> specs/[FEATURE]/checklist.md << EOF

## Evidence Requirements

**For Each Checklist Item**:
- [ ] Clear pass/fail criteria defined
- [ ] Evidence of completion documented
- [ ] Links to supporting documentation provided
- [ ] Testing results or validation proof included
- [ ] Stakeholder approval obtained where required

**Constitutional Evidence**:
- [ ] Template usage logs show standardization compliance
- [ ] Version control history demonstrates governance process
- [ ] Decision rationale documented for any deviations
- [ ] Approval chain maintained for constitutional compliance
EOF
```

**Step 4.2: Validation Process Definition**
```bash
# Define validation process for checklist completion
cat >> specs/[FEATURE]/checklist.md << EOF

## Validation Process

### Pre-Implementation Validation
- [ ] Specification checklist verified before planning begins
- [ ] Planning checklist completed before implementation starts
- [ ] Task checklist reviewed before development begins

### Post-Implementation Validation
- [ ] Implementation matches specification requirements
- [ ] All quality gates pass successfully
- [ ] Constitutional compliance verified
- [ ] Stakeholder approval obtained

### Governance Validation
- [ ] All constitutional principles followed
- [ ] Template standardization maintained
- [ ] Documentation requirements satisfied
- [ ] Version control practices applied
EOF
```

### Phase 5: Checklist Finalization

**Constitutional Alignment**: Template Standardization

**Step 5.1: Metadata and Tracking**
```bash
# Complete checklist metadata
cat >> specs/[FEATURE]/checklist.md << EOF

## Checklist Status

**Generated**: $(date)
**Type**: $CHECKLIST_TYPE
**Total Items**: [Count] verification items
**Constitutional Compliance**: Required
**Approval Required**: Yes
**Evidence Required**: Yes

## Notes

- Check items off as completed: \`[x]\`
- Add comments or findings inline
- Link to relevant resources or documentation
- Items are numbered sequentially for easy reference (CHK001, CHK002, etc.)
- All items must be verified before feature deployment
EOF
```

**Step 5.2: Integration with Development Workflow**
- [ ] Checklist linked to feature specification
- [ ] Verification points integrated with task list
- [ ] Quality gates defined in development process
- [ ] Approval workflow documented

**Step 5.3: Maintenance and Updates**
- [ ] Checklist reviewed when requirements change
- [ ] Items updated for new constitutional requirements
- [ ] Evidence collection process maintained
- [ ] Historical compliance records preserved

## Output Artifacts

The `/speckit.checklist` command generates:

1. **`checklist.md`** - Comprehensive verification checklist with numbered items
2. **`.checklist-metadata`** - Checklist tracking and version information
3. **Category Organization** - Logical grouping of verification items
4. **Evidence Requirements** - Clear documentation of what constitutes proof of completion
5. **Validation Process** - Defined process for checklist verification and approval

## Error Handling

**Constitutional Violations**:
- Missing or incomplete input specifications
- Template standardization not followed in source documents
- Feature independence requirements not met
- Documentation-First approach not applied

**Technical Issues**:
- Invalid feature directory structure
- Template parsing or generation failures
- Metadata creation errors
- Category definition conflicts

## Integration Points

**Constitutional Governance**:
- Checklist generation enforces template standardization
- Verification process ensures constitution-driven governance
- Evidence collection supports compliance auditing
- Quality gates maintain constitutional compliance

**Development Workflow**:
- Checklists serve as quality gates in development process
- Verification items guide testing and validation efforts
- Evidence requirements ensure proper documentation
- Approval process integrates with governance workflow

## Success Criteria

**Checklist Success**:
- [ ] All verification categories appropriately covered
- [ ] Items are specific, actionable, and verifiable
- [ ] Evidence requirements clearly defined
- [ ] Integration with development workflow established
- [ ] Ready for use in quality assurance process

**Constitutional Compliance**:
- [ ] Template standardization maintained throughout
- [ ] Constitution-driven governance supported
- [ ] All five constitutional principles verified
- [ ] Governance requirements satisfied
- [ ] Quality assurance process enhanced

## Next Steps

After successful checklist generation:

1. **Review and Customization**: Adapt generated checklist for specific feature needs
2. **Integration**: Incorporate checklist into development workflow
3. **Verification Planning**: Plan evidence collection and validation process
4. **Approval Process**: Establish governance approval workflow
5. **Maintenance**: Keep checklist updated as requirements evolve

---

**Constitution Version**: 1.0.0 | **Template Version**: 1.0.0 | **Last Updated**: 2025-10-07