# Micro Moves

This mobile app is designed to promote better health by reminding users to take small, simple exercises at periodic intervals. Whether it's for office workers who sit for long hours, elderly individuals who need gentle stretches, or anyone looking to improve their well-being, this app provides timely reminders for activities like stretching, breathing exercises, and more. The goal is to encourage users to incorporate micro breaks into their daily routine for improved health and wellness.

## Design principles

1. **Simple & minimal**: The app should have a clean and intuitive interface that allows users to easily set up reminders and access exercise suggestions without any confusion. Minimalism should be used to provide pleasant, peaceful and intuitive. For example, lines and spaces used carefully to design. Take inspiration from Dieter Rams (the designed)
2. **Elderly-friendly**: The app should be designed with accessibility in mind, ensuring that it is easy to use for elderly users who may have limited tech experience.
3. **Peaceful**: The app should use calming colors and gentle notifications to create a peaceful experience that encourages users to take breaks without feeling overwhelmed or stressed.

## Usability & accessibility

- The app should be designed to be user-friendly and accessible to a wide range of users, including those with limited technical skills. This can be achieved by using large buttons, clear instructions, and simple navigation. Additionally, the app should offer customizable reminder settings, allowing users to choose the frequency and type of exercises that best suit their needs. Accessibility features such as voice commands or compatibility with screen readers can further enhance the user experience for elderly individuals or those with disabilities.
- Consistency should be maintained in a way that users can easily understand and navigate the app. This includes using familiar icons, consistent color schemes, and a straightforward layout that minimizes cognitive load. The app should also provide clear feedback when users set reminders or complete exercises, reinforcing positive behavior and encouraging continued use.
- User should be able to access the app in one hand with minimal clicks. User should learn to use the app in less than 5 mins.

## Color & mood

The color palette for the app should consist of soft, calming colors such as light blues, greens, and pastels. These colors can help create a serene and inviting atmosphere that encourages users to take their breaks and engage in the suggested exercises. The overall mood of the app should be tranquil and supportive, fostering a sense of well-being and relaxation.

## Name prompt

suggest name for mobile app which helps the user by reminding small and simple exercises in periodic intervals. For example, for person sitting the chair for the whole day for work to remind to get up, do simple flexibility stretches. Same for elderly people to remind periodic stretches and remind people at intervals to do breathing exercise and more similar. So the goal is remind people softly to do small but periodic exercises for better health.

## Pages/Screens

### Breaks List

First screen user sees after opening the app. It shows the list of breaks which are active currently. It shows the break name, when is the next trigger for this break(For example 'in 15 mins'), and a simple CTA to pause the break for next x times. Breaks are ordered by next occurring order.
Another simple CTA to take user to next screen used to change the active breaks (enable/disable).

### Customize Breaks

User views the available list of breaks. It shows basic details about break like enabled or not, frequency & active time range as summary card.
By tabbing on it, the break card expands & they will be able to customize the break in same screen by

1. Enable/disable
2. Frequency of break
3. From when to when it will be enabled
   The screen has CTA to create new break in different screen.

### New Break

User can create a new break definition. In this screen, user can add/customize below details.

1. Name
2. Summary/Description of break - User explain what this break is all about and how it helps.
3. Break's Visual Design. It will be designed like deck of slides to be shown during break. For example, Palming eye exercise. Each slide will have
   3.1 Text of step (Look away from computer screen)
   3.2 Image to be shown during the step (User can attach an image where user looking away from computer screen)
   3.3 time period to show the slide for this step
4. Trigger pattern (When break time reaches how the user want to be notified. For example, single beep or lengthy ring or screen blink or combination of these)
5. Default values for frequency of break and time range to be active

### Break

App shows this when break time reaches. It be a slide show in auto time mode (or touch to move to next slide) which steps of exercise they need to do during the break. It will show an image shows pictorial representation of what the user need to do as part of the exercise step and short text at bottom explaining what to do. If no image, text is shown bigger.
User must be able to skip the break. But make this option subtle as we do not encourage them to skip it. They also need to long press the CTA to skip.

### Initial setup screens

App shows the screens which guides the users through the series of steps to provide required access like Exact Alarms, Full Screen Intent, & etc. Then finally lands in the manage breaks screen where can they can enable any breaks they want to.

## Alternative names

- Pausify
- Nudgy
- NudgeWell
