# Pelago Android Challenge

<details>
<summary> Original Readme </summary>
This is a skeleton project for our Android coding challenge, based around the following tech stack:

- Kotlin
- MVVM
- Jetpack Compose
- Coroutines
- Hilt
- Retrofit

Feel free to change the project structure as you see fit, or add any dependencies you think are necessary to complete
the challenge.

## Challenge

Given the acceptance criteria below, you should identify the missing parts of the code and implement them.

While the feature itself is pretty simple, we encourage you to be creative and demonstrate your expertise when it comes
to architecture, user experience, and overall best practices.
We understand working on such a task can be pretty time consuming so your solution doesn't have to be perfect, but it
should be good enough to give us an idea of your skills and how you approach a task.

## Acceptance criteria

### Scenario 1

**Given** I am on the home screen \
**When** The screen is first loaded \
**Then** A random fact is displayed

### Scenario 2

**Given** I am on the home screen \
**When** I press on the "More facts!" button \
**Then** A new random fact is displayed

### Scenario 3

**Given** I am on the home screen \
**When** I press on the "More facts!" button \
**Then** The previously displayed fact gets added to a list* \
**And** The list should be limited to the last 3 facts (with newer facts replacing older ones)

*The list should be displayed below the "More facts!" button

### Scenario 4

**Given** I am on the home screen \
**When** I swipe (left or right) on a list item \
**Then** That fact is removed from the list

## Resources

- [Random facts API Documentation](https://uselessfacts.jsph.pl/)

## Submission

You can either send us a link to a public repository (GitHub, Bitbucket, etc.) or a zip file with your project when
you're done.
</details>

## Features
- Included all Acceptance Criteria
- Haptic feedback for swipe to delete
- Supports undo swipe-to-delete
- Supports both Portrait and Landscape
- Main fact is scrollable in case the text is too long
- Fact list will auto scroll to top when new item is added
- Single tap on main fact or fact list item will show a snack bar with tips
- Long tap on main fact or fact list item will launch browser to open the URL from the fact

## Tests
- Unit tests included 
- UI tests included