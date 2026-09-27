# quadruplet
Feature-based chart parser written in Kotlin
Now with added CI


Modeled on NLTK's FeatureParser.

## Scope

Feature values may be atoms, `?x` variables, lists and tuples, or
semantic terms in `<...>`. **Nested feature maps (a feature whose value is
itself a feature map) are not supported, and there are no current plans to
support them.**

* The `FeatureNotation` notation (`FeatureTerms.g4`) cannot express them.
* The `IntegratedParser` notation (`FeatParser.g4`) accepts them
  syntactically, but no grammar or test uses them and they are not tested.
* The unifier happens to handle them structurally, but nothing relies on
  that and it may change.

Unification is term unification with named variables: reentrancy is written
by repeating a variable, e.g. `S[num=?n] -> Np[num=?n] Vp[num=?n]`.

A Go prototype of the chart parser core, used to explore parallel parsing,
is in [`go/`](go/README.md).





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
