# Suggested commit plan (genuine, incremental development)

The assignment requires **10+ real commits over the course of development** (history is checked against your video).
Do NOT commit everything at once. Build and run after each step, then commit with the message shown:

1. `Create Android Studio project skeleton with Material theme and Gradle config`
2. `Add Ingredient, Recipe, RecipeIngredient and MatchResult model classes`
3. `Implement DatabaseHelper with pantry table and CRUD methods`
4. `Add pantry list screen layout with RecyclerView and PantryAdapter`
5. `Add Add/Edit ingredient screen with input validation`
6. `Wire Create/Update/Delete from pantry list (confirm dialog + undo)`
7. `Add recipes tables and seed 22 recipes on first run`
8. `Implement IngredientNormalizer and UnitConverter (plural/unit robustness)`
9. `Implement strict RecipeMatcher and Suggested Recipes screen`
10. `Add recipe detail screen with ingredient tick/cross and method`
11. `Add bottom navigation and Settings screen with SharedPreferences`
12. `Add expiring-soon banner and prioritise recipes using expiring food`
13. `Add Almost There tab (bonus) separate from strict matches`
14. `Add JUnit tests for strict matching rule`
15. `Fix empty-state messages and polish layouts`
16. `Add README with database justification and run instructions`

Tip: use `git add -p` / commit as you finish each piece over several days, and `git push` regularly.
Make the repository PUBLIC (or add the marker as a collaborator) and paste the link in your report.
