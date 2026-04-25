# Elevate Functional Pages UI Design

## Goal

Upgrade 5 functional pages (Interview, Resume, VoiceInterview, Schedule, Knowledge) to match HomePage's premium glassmorphism aesthetic.

## Visual Language

### Glassmorphism Cards
- Semi-transparent background: `rgba(255,255,255,0.08)` dark / `rgba(255,255,255,0.7)` light
- Backdrop blur: `16-24px`
- Border: `1px solid rgba(255,255,255,0.1)`
- Shadow: `0 8px 32px rgba(102, 126, 234, 0.15)`
- Border radius: `12-16px`

### Gradient Accents
- Primary: `linear-gradient(135deg, #667eea 0%, #764ba2 100%)`
- Success: `linear-gradient(135deg, #11998e 0%, #38ef7d 100%)`
- Warning: `linear-gradient(135deg, #f093fb 0%, #f5576c 100%)`

### Typography
- Page titles: `24px, font-weight 600`
- Section headers: `18px, font-weight 500`
- Card titles: `16px, font-weight 500`
- Body: `14px`
- Captions: `12px, muted`

---

## Animated Stat Cards

### Structure
```
┌─────────────────────────────────┐
│  [Gradient Icon]                │
│  ████████████  Progress bar     │
│  128          Large number      │
│  Total Sessions Label           │
│  +12% ↑     Trend indicator     │
└─────────────────────────────────┘
```

### Animations
- Number counts up on mount
- Progress bar fills with ease-out transition
- Subtle pulse glow on icon
- Hover: lift 4px, glow intensifies

### Per-Page Stats

| Page | Stat 1 | Stat 2 | Stat 3 | Stat 4 |
|------|--------|--------|--------|--------|
| Interview | Total Sessions | Completed | Avg Score | Today |
| Resume | Uploaded | Analyzed | Pending | This Week |
| VoiceInterview | Sessions | Paused | Active | Duration |
| Schedule | Scheduled | Completed | Upcoming | This Month |
| Knowledge | Documents | Categories | Queries | Last Update |

### Icon Mapping
- Sessions/Total: Calendar (purple-blue gradient)
- Completed/Analyzed: Check circle (green gradient)
- Pending/Paused: Clock (orange gradient)
- Score/Duration: Star/Timer (pink-purple gradient)

---

## Implementation Approach

### Shared CSS Classes (dashboard.css additions)
- `.glass-card` - base glassmorphism
- `.stat-card-animated` - animated stat card
- `.stat-icon-gradient` - gradient icon wrapper
- `.stat-number` - count-up number
- `.stat-progress-bar` - animated progress

### Per-Page Changes
1. **InterviewPage.vue** - 4 animated stats, glass card sections
2. **ResumePage.vue** - 4 animated stats, glass upload area, refined table
3. **VoiceInterviewPage.vue** - 4 animated stats, glass timeline
4. **SchedulePage.vue** - 4 animated stats, glass form, glass calendar
5. **KnowledgePage.vue** - 4 animated stats, glass Q&A section

---

## Files to Modify
- `interview-frontend/src/styles/dashboard.css` - Add shared classes
- `interview-frontend/src/pages/InterviewPage.vue`
- `interview-frontend/src/pages/ResumePage.vue`
- `interview-frontend/src/pages/VoiceInterviewPage.vue`
- `interview-frontend/src/pages/SchedulePage.vue`
- `interview-frontend/src/pages/KnowledgePage.vue`
