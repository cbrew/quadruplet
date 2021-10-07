# quadruplet
Feature-based chart parser written in Kotlin
Now with added CI


Modeled on NLTK's FeatureParser.





<!--
<template id="todos">
    <div>
        <h1>{{name}}</h1>
        <form method="post" action="/msg">
            <input name="text" v-model="msg" style="width: 500px;" @keyup.enter="show">
        </form>


        <div class="parses" style="width: 500px">

        <ul class="edges"  style="width: 500px">
            <li v-for="edge in edges">
                {{edge.start}}-{{edge.label}}-{{edge.end}} <br/>
            </li>
        </ul>
        <p></p>
          <table>
              <tr>
                <td class="word" v-for="word in words">{{word.label}}

                    <span style="font-size: 0.8em; font-weight: bold; line-height: 1; border-radius: 0.35em; text-transform: uppercase; vertical-align: middle; margin-left: 0.5rem">PERSON</span>

                </td>
              </tr>
          </table>

        </div>
    </div>
</template>
<script>

        var p = Vue.component("todos", {

        template: "#todos",
        data: () => ({
            msg: "I want an umbrella",
            edges: [],
            name: "Rich NLP",

        }),
       });
</script>
<style>
    .word {
            color: blue;
            background-color: white;
            border: 1px dotted blue;
            }

</style>
