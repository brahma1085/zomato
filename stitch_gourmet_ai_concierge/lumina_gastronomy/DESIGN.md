---
name: Lumina Gastronomy
colors:
  surface: '#0b1326'
  surface-dim: '#0b1326'
  surface-bright: '#31394d'
  surface-container-lowest: '#060e20'
  surface-container-low: '#131b2e'
  surface-container: '#171f33'
  surface-container-high: '#222a3d'
  surface-container-highest: '#2d3449'
  on-surface: '#dae2fd'
  on-surface-variant: '#cbc3d7'
  inverse-surface: '#dae2fd'
  inverse-on-surface: '#283044'
  outline: '#958ea0'
  outline-variant: '#494454'
  surface-tint: '#d0bcff'
  primary: '#d0bcff'
  on-primary: '#3c0091'
  primary-container: '#a078ff'
  on-primary-container: '#340080'
  inverse-primary: '#6d3bd7'
  secondary: '#4edea3'
  on-secondary: '#003824'
  secondary-container: '#00a572'
  on-secondary-container: '#00311f'
  tertiary: '#ffb95f'
  on-tertiary: '#472a00'
  tertiary-container: '#ca8100'
  on-tertiary-container: '#3e2400'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e9ddff'
  primary-fixed-dim: '#d0bcff'
  on-primary-fixed: '#23005c'
  on-primary-fixed-variant: '#5516be'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#0b1326'
  on-background: '#dae2fd'
  surface-variant: '#2d3449'
typography:
  display-lg:
    fontFamily: Hanken Grotesk
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Hanken Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-caps:
    fontFamily: Geist
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  mono-data:
    fontFamily: Geist
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  container-margin: 24px
  gutter: 16px
  section-gap: 64px
---

## Brand & Style
The design system embodies a premium, AI-forward intelligence tailored for high-end restaurant discovery. The aesthetic is rooted in **Modern Glassmorphism** with a focus on depth, transparency, and technical precision. The goal is to evoke a sense of effortless expertise—as if the UI is "thinking" alongside the user. 

The interface relies on heavy whitespace (negative space) and high-quality typography to ensure the imagery of the food remains the hero. Visual interest is generated through light-refraction effects, subtle background blurs, and vibrant accent colors that represent the "neural" layer of the platform.

## Colors
The palette is centered on a deep "Midnight Slate" (`#0F172A`) to provide a sophisticated, cinematic backdrop for food photography.

- **AI Violet (Primary):** Used for intelligent features, AI matching, and primary actions. It represents the "brain" of the platform.
- **Intelligent Emerald (Secondary):** Used for success states, sustainability markers, and "Open Now" indicators.
- **Saffron Glow (Tertiary):** A warm, appetizing accent used sparingly for ratings, premium badges, and culinary highlights.
- **Glass Overlays:** Surfaces use a semi-transparent white or slate with a `backdrop-filter: blur(12px)` to create the signature glassmorphic look.

## Typography
The typographic hierarchy emphasizes clarity and data precision. 

- **Hanken Grotesk** is used for headlines to provide a sharp, contemporary edge that feels "designed."
- **Inter** handles the body copy for maximum legibility when reading long descriptions or menus.
- **Geist** is reserved for technical data points (e.g., match percentages, price ranges, distances) to lean into the "AI-first" developer aesthetic.
- Use tighter letter-spacing on large headlines to maintain a premium, editorial feel.

## Layout & Spacing
The layout follows a **Fluid Grid** model with generous internal padding to create a sense of luxury.

- **Desktop:** 12-column grid with 24px gutters. Content is centered with a max-width of 1280px.
- **Mobile:** Single column with 20px side margins. 
- **Rhythm:** Use multiples of 8px for all spacing. For glassmorphic cards, use a minimum of 24px internal padding to allow the blurred background effects to breathe.
- **Reflow:** On tablet, 12 columns collapse to 8, and horizontal scrolling is used for "Recommended" or "Discovery" carousels.

## Elevation & Depth
This design system avoids traditional drop shadows in favor of **Tonal Elevation** and **Backdrop Blurs**.

- **Level 1 (Base):** Midnight Slate background.
- **Level 2 (Cards):** Surface color at 40% opacity with a 1px inner border (stroke) at 10% white to simulate the edge of glass.
- **Level 3 (Popovers/Modals):** Surface color at 60% opacity with a 32px backdrop blur and a soft, wide-spread shadow (`y-20, blur-40, opacity-0.3`).
- **Shimmer:** All loading states use a diagonal linear-gradient shimmer (Violet to Transparent) to indicate active AI processing.

## Shapes
The shape language is sophisticated and modern. 

- **Standard Elements:** Use `rounded-lg` (16px) for standard UI components.
- **Primary Cards:** Use `rounded-xl` (24px) for restaurant cards and featured discovery content to create a soft, inviting container.
- **Pills/Indicators:** Use full-round (pill) shapes for status badges, AI match percentages, and category filters to contrast against the more structured cards.

## Components

### AI Match Progress Ring
A circular indicator using a conic gradient of **AI Violet** and **Intelligent Emerald**. The center should remain transparent to show the underlying imagery or glass background. The percentage text uses **Geist** for a technical feel.

### Glassmorphic Cards
Cards feature a 1px "Light Leak" border on the top and left sides. The background is a mix of `rgba(255, 255, 255, 0.05)` and a heavy blur. Images within cards should have a subtle dark-to-transparent gradient overlay at the bottom to ensure white text legibility.

### AI Action Buttons
Primary buttons use a vibrant **AI Violet** gradient with a subtle outer glow. Hover states should trigger a "pulse" animation where the glow expands slightly, simulating tactile feedback.

### Interactive Pills
Filter pills should be ghost-styled (transparent background, thin border) when inactive, and fill with a solid **Saffron** or **Emerald** tint when active.

### Inputs
Search fields are oversized with a 20% opacity white background and a "Search AI..." placeholder. Use a subtle shimmer effect inside the input border to indicate the "Live AI" listening state.