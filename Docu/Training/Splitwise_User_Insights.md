## Why People Use Splitwise

Splitwise is the de-facto "social ledger" for friends, couples and house-mates who constantly share costs.  A few recurring pain-points drive adoption:

1. **Uneven reimbursements are hard to track** – people forget who paid last, lose receipts or settle the wrong amount.
2. **Traditional spreadsheets feel heavy** – writing formulas for every pizza hurts, especially on mobile.
3. **Cash & bank transfers are asynchronous** – payment may happen days after the purchase; a running balance is needed.
4. **Conversation killer** – nobody enjoys talking about money after a fun activity; an app removes the awkwardness.

---

### Core User Journeys / "Aha!" moments

| Situation | Splitwise Interaction | Value Delivered |
|-----------|----------------------|-----------------|
| Weekend trip with 4 friends | *Add Expense → "Paid by Alex, split equally"* | Everyone immediately sees who's owed what; no mental math. |
| Room-mates utility bill | *Recurring expense → Auto-reminder* | Removes the monthly chore of re-entering the same bill. |
| Couple managing groceries | *Percentage split (60/40)* | Mirrors their income ratio – feels fair and flexible. |
| Large group dinner | *Scan receipt → "Split by shares"* | Tally items quickly; avoids chaos at the table. |

---

### UX Principles That Make It Intuitive

1. **Conversation Language** – Phrases such as *"Paid by you and split equally"* read like a sentence, reducing cognitive load.
2. **Progressive Disclosure** – The first screen only asks for amount & description.  Advanced options (currency, tax, shares) hide behind lightweight chips.
3. **Pill Buttons as State** – The *Paid by* and *Split* chips show the current selection *and* invite the user to tap to change – no extra labels needed.
4. **Single-screen Flow** – Adding an expense never requires switching tabs; the keyboard stays visible so the flow is fast.
5. **Running Balance Banner** – A persistent summary keeps the mental model ("I owe Sarah ₱200") without diving into details.

---

### Layout Insights (derived from screenshot)

```
TopAppBar   ← back    "Add expense"               ✔
Friend Row  "With you and:  [recipient field]"
Content     ┌ icon  Description field ┐
            ├ icon  Amount field      ┤
Sentence    "Paid by  [chip]  and split  [chip]"

( Additional keypad / attachments live at the bottom )
```

The hierarchy follows the natural conversation order: *Who? What? How much? Who pays?*.
The keypad stays visible, reinforcing the primary task – entering the number.

---

### Take-aways for Fairr

*  Keep the calculator – it's a signature productivity booster.
*  Adopt the conversational chips; they educate and act as shortcuts.
*  Minimise dropdowns; default to smart guesses (e.g., last payer, equal split).
*  Place secondary actions (category, attachments) at the periphery.

Implementing these patterns will make Fairr's "Add Expense" feel lighter, faster and friendlier. 