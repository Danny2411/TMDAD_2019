for /l %%x in (1, 1, %1) do (
   start chrome "http://localhost:4567/?u=d" -incognito
)