# SortAI — AI-Powered E-Waste Classification, Safe Sorting & Recycler Bidding Marketplace

SortAI empowers informal scrap collectors (kabadiwalas), scrap shop aggregators, and authorized recyclers to safely classify discarded electronics, eliminate hazardous contamination, and maximize material recovery value through an end-to-end computer vision and bidding ecosystem.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following core architectural decisions were confirmed during Phase 1:
> - **Vision Engine Mode**: Hybrid mode combining cloud-powered **Gemini 3.5 Flash Multimodal Vision** with a deterministic **offline rule-based computer vision classifier** and a pre-loaded sample scrap image testbed for zero-latency testing.
> - **Multi-Role Demonstration**: Top-level instant role switcher toolbar allowing seamless real-time switching between **Collector**, **Shop Owner**, **Recycler**, and **Administrator** perspectives without repetitive re-logins.
> - **Accessibility & Language**: Native **Hindi (हिन्दी)** and **English** localization with integrated Android **Text-to-Speech (TTS)** voice announcements for safety warnings and crate directions.

---

### 1. Overview & Core Concept

- **What It Does**: A collector captures or selects a photo of e-waste (whole device or disassembled component). The app classifies the material into standardized categories, assesses hazard levels (lithium-ion batteries, swollen cells, high-voltage transformers), directs the item into a specific color-coded crate with spoken voice guidance, tracks verified inventory, flags threshold triggers, and connects accumulated lots to competitive bids from certified recyclers.
- **Target Personas**:
  1. *Informal Collectors & Kabadiwalas*: Need high-contrast visual icons, simple audio guidance in Hindi/English, and clear hazard alerts without technical recycling jargon.
  2. *Scrap Shop Owners & Aggregators*: Need inventory volume tracking, crate threshold alerts, and batch lot publishing.
  3. *Authorized Recyclers*: Need category-specific lot discovery, transparent purity and weight specifications, and bid placement with scheduled pickup logistics.
  4. *Platform Administrators*: Oversee audit logs, dispute resolutions, and recycler authorization credentials.
- **Key Value**: Prevents battery fires, stops toxic mercury/lead mishandling, segregates high-value PCBs and copper before scrap downcycling, and guarantees transparent price discovery.

---

### 2. User Experience & Visual Design

#### The 6 Color-Coded Crate Standard & Palette
The app adheres strictly to the official SortAI color taxonomy paired with tactile iconography and audio cues:

| Crate | Category | Color Accent | Hex Token | Icon |
|---|---|---|---|---|
| **Crate 1** | Printed Circuit Boards (PCBs) | Deep Green | `#2E7D32` | Circuit / Memory |
| **Crate 2** | Batteries (Hazardous) | Caution Red | `#C62828` | Battery Alert / Danger |
| **Crate 3** | Copper Coils, Transformers & Motors | Industrial Orange | `#EF6C00` | Electric Bolt / Coil |
| **Crate 4** | Cables & Wires | Warning Amber | `#F57F17` | Cable / Cord |
| **Crate 5** | Display Panels (CRT / LCD / OLED) | Tech Blue | `#1565C0` | Tv / Monitor |
| **Crate 6** | Glass & Engineered Plastics | Royal Purple | `#6A1B9A` | Recycling / Layers |
| **Review** | Unknown / Needs Inspection | Neutral Slate | `#546E7A` | Help / Warning |

#### Key User Flows
1. **Collector "Snap-to-Sort"**:
   - Opens Camera or picks from Scrap Gallery / Demo Testbed.
   - Selects mode: *Whole Device* (phone, laptop, TV, microwave) or *Separated Component* (board, motor, cell, cable).
   - Real-time classification card displays predicted category, confidence score, safety banners, and target crate color.
   - Android Text-to-Speech speaks aloud in Hindi or English (e.g., *"लिथियम बैटरी मिली। सावधान! क्रेट 2 (लाल) में रखें"*).
   - Collector confirms or overrides category and enters weight/quantity.
2. **Crate & Lot Management**:
   - Displays real-time crate fill percentage against category pickup thresholds (e.g., 25 kg for PCBs, 50 kg for copper motors).
   - One-tap "Create Material Lot" when threshold is met.
3. **Recycler Bidding & Pickup**:
   - Recycler views eligible category lots, submits per-kg bids with pickup time windows.
   - Collector compares offers and accepts the best bid.
   - Pickup lifecycle transitions: *Open for Bids* → *Bid Accepted* → *Pickup Scheduled* → *Collected* → *Purity & Weight Verified* → *Demo Payment Recorded*.
4. **Admin Dashboard**:
   - Platform transaction metrics, recycler verification approvals, hazard incident logs, and database reset controls.

---

### 3. Key Product Decisions & Trade-Offs

- **Hybrid AI Inference Architecture**:
  - *Primary*: Gemini 3.5 Flash Multimodal REST API analyzes the image with structured JSON output returning category, confidence, component breakdown, hazard status, and recommended crate.
  - *Fallback / Offline*: Built-in heuristic computer vision pattern matcher with pre-bundled sample scrap dataset for immediate zero-connectivity operation.
  - *Safety Policy*: Strict threshold gating. Suspected swollen batteries or unknown components automatically route to the "Needs Review" or "Hazard Warning" state.
- **Embedded Persistence (Room SQLite)**:
  - Robust, reactive multi-table schema (`scans`, `crates`, `material_lots`, `bids`, `pickups`, `transactions`, `user_corrections`).
  - Survives app restarts, orientation changes, and emulator reloads while providing instant local queries via Kotlin Coroutines and `Flow`.
- **Demo-Friendly Role Switching**:
  - A sticky top utility bar allows evaluators to toggle between Collector, Shop Owner, Recycler, and Admin in one click, demonstrating the complete multi-stakeholder loop on a single device.
- **Simulated Financial & Purity Verification**:
  - Clearly stamped "Demo Payment — Academic Simulation" with generated transaction references, weight reconciliation, and purity adjustments.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        SortAI Android Client                           │
│    (Jetpack Compose + Material 3 + Android TTS + Camera/Picker)        │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
    ┌───────────────────────────────┴───────────────────────────────┐
    │                                                               │
    ▼                                                               ▼
┌─────────────────────────────┐                 ┌─────────────────────────────┐
│    SortAI Repository Layer  │                 │    Vision & Inference Engine│
│  (Crates, Lots, Bids, etc.) │                 │  - Gemini 3.5 Flash Vision  │
└──────────────┬──────────────┘                 │  - Local Heuristic Fallback │
               │                                │  - Built-in Scrap Testbed   │
               ▼                                └─────────────────────────────┘
┌─────────────────────────────┐
│    Room SQLite Database     │
│  - Items & Scanned Entities │
│  - Crate Inventories        │
│  - Lots, Bids & Pickups     │
│  - Audit & Corrections Logs │
└─────────────────────────────┘
```

#### Core Data Entities
1. **ScannedItem**: `id`, `category`, `predictedClass`, `confirmedClass`, `confidence`, `isHazard`, `hazardNote`, `crateId`, `weightKg`, `quantity`, `imageUrl`, `timestamp`.
2. **CrateState**: `crateId`, `categoryName`, `colorHex`, `currentWeightKg`, `thresholdKg`, `itemCount`, `isThresholdReached`.
3. **MaterialLot**: `lotId`, `category`, `totalWeightKg`, `reservePricePerKg`, `status` (*OPEN*, *BID_ACCEPTED*, *SCHEDULED*, *COMPLETED*).
4. **RecyclerBid**: `bidId`, `lotId`, `recyclerName`, `pricePerKg`, `totalAmount`, `proposedDate`, `status` (*PENDING*, *ACCEPTED*, *REJECTED*).
5. **PickupRecord**: `pickupId`, `lotId`, `bidId`, `verifiedWeightKg`, `purityScore`, `finalAmount`, `status`, `paymentRef`.

---

### 5. Verification Plan

1. **Unit & Logic Tests**:
   - `testDebugUnitTest` with Robolectric validating Room DAO transactions, threshold detection, and bid state machine transitions.
2. **Build & Compilation Verification**:
   - `compile_applet` to ensure error-free compilation and clean packaging.
3. **CUJ Walkthroughs**:
   - Scan scrap image → verify category + color crate assignment + audio speech playback.
   - Accumulate crate inventory → trigger threshold → publish marketplace lot.
   - Switch to Recycler → place bid → switch to Collector → accept bid.
   - Complete pickup verification with purity rating and demo payment receipt.
