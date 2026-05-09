# Design Context: Micro Moves (The Wellness Extension)

**The Mission**: A minimalist health utility designed as a "soft companion" for office workers and the elderly. It bridges the gap between sedentary work and physical well-being through quiet, periodic exercise reminders.
**The Philosophy**: Inspired by Dieter Rams' "Less, but better." No decoration—only utility. The interface should feel like a physical, tactile object that lives on the phone.
**Technical Foundation**: All designs must strictly adhere to the provided CSS theme (muted greens, off-whites, 16px radii, and Inter typography).

---

# Screen Instruction: Dashboard (Breaks List)

## 1. Scene Composition
* **Header**: Place the name "Micro Moves" in the top-left using `--foreground`. The font weight should be medium, and the size should be subtle (around 20pt). No icons in the header.
* **Main List**: A vertical scrollable area containing **Break Cards**.
* **Bottom Anchor**: A fixed primary button at the very bottom of the screen labeled "Manage Breaks" (Width: 100% minus padding, Height: 60px). Use `--primary` background and `--primary-foreground`.

## 2. Component Blueprint: The "Break Card"
Each card represents a scheduled interval. Design the cards to show two distinct states on this single screen:

### State A: Active Interval (The standard view)
* **Top 3 Cards**: Show these as active.
* **Visuals**: Use `--card` background with `--shadow`. 
* **Content**: 
    * **Break Title**: (e.g., "Palming Eye Exercise") in `--foreground` (Bold).
    * **Timer**: Directly to the right of the title, show "in 12 mins" using `--primary` color.
    * **CTA**: A button on the right side labeled "Pause." Use `--secondary` background with `--secondary-foreground`.

### State B: Paused Interval (The inactive view)
* **Bottom Card**: Depict the very last card in the list as "Paused."
* **Visuals**: Change the card background to `--muted`. Reduce the opacity of the entire card to 60% to show it is inactive.
* **Content**: 
    * **Break Title**: "Shoulder Rolls" (or similar).
    * **Status Text**: Instead of a timer, show "Paused for 2 cycles" in `--muted-foreground`.
    * **CTA**: The button text changes to "Resume" and remains subtle.

## 3. Spatial & Accessibility Constraints
* **One-Handed Zone**: Ensure the "Manage Breaks" button and the "Pause/Resume" CTAs are easily reachable within the bottom half of the screen.
* **Negative Space**: Maintain exactly 20px padding between each card and 24px horizontal margins from the screen edges.
* **Empty State (Reference only)**: If the list were empty, a single centered icon of a breathing circle would appear. (Do not render this now; prioritize the list).

---

**Instruction to AI**: *Generate a clean, high-fidelity mobile UI of this dashboard. Focus on the contrast between the vibrant "Active" cards and the ghosted "Paused" card to demonstrate the system's logic.*