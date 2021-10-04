<template id="parser">
    <div>
    <h1 class="parser">{{sentence.text}}</h1>
    <table>
        <tr><td colspan="4">S</td></tr>
    <!-- 0 [Complete(category=S[sem=<∃.((product_type(umbrella, q:1) ∧ need(speaker, q:1)))>], start=0, end=4)] -->
        <tr><td colspan="1">NP</td><td colspan="3" class="focus" >VP</td> </tr>
        <!-- 0 1 [Complete(category=NP[sem=<λ.(v:1(speaker))>], start=0, end=1), Complete(category=VP[sem=<λ.(∃.((product_type(umbrella, q:1) ∧ need(v:1, q:1))))>], start=1, end=4)] -->
        <tr><td colspan="1">Pn</td><td colspan="1" >Tvb</td><td colspan="2">NP</td> </tr>
        <!-- 2 [Complete(category=Pn[sem=<λ.(v:1(speaker))>], start=0, end=1), Complete(category=Tvb[sem=<λ.(λ.(v:2(λ.(need(v:2, v:1)))))>], start=1, end=2), Complete(category=NP[sem=<λ.(∃.((product_type(umbrella, q:1) ∧ v:1(q:1))))>], start=2, end=4)] -->
        <tr><td colspan="2" style="border:none"></td><td>Det</td><td>NBAR</td></tr>
        <!--  3 [Complete(category=Det[sem=<λ.(λ.(∃.((v:2(q:1) ∧ v:1(q:1)))))>], start=2, end=3), Complete(category=NBAR[sem=<λ.(product_type(umbrella, v:1))>], start=3, end=4)] -->
        <tr><td colspan="3" style="border:none"></td><td>N</td></tr>
        <!-- 4 [Complete(category=N[sem=<λ.(product_type(umbrella, v:1))>], start=3, end=4)]-->
        <tr><td>I</td><td>want</td><td>an</td><td>umbrella</td></tr>
        <!-- Sentence; [Span(label=I, start=0, end=1), Span(label=need, start=1, end=2), Span(label=an, start=2, end=3), Span(label=umbrella, start=3, end=4)] -->

        </table>
        <ul class="edges">
            <li v-for="edge in edges">
                {{edge.start}}-{{edge.label}}-{{edge.end}} <br/>
            </li>
        </ul>

        </div>
    </template>
    <script>
        Vue.component("hello-world", {

        template: "#parser",
        data: () => ({
            edges: [],
            sentence: []
        }),
        created(){
            // obtain the sentence from the back end.
            fetch("/sentence")
            .then(response => response.json())
            .then(data => (this.sentence = data));

            // obtain the edges from the back end.
            fetch("/edges")
            .then(response => response.json())
            .then(data => (this.edges = data));
        }
        });
    </script>
    <style>
        .parser {
            color: blue;
        }
        .edges {
            color: blue;
            }
        .focus {
            background-color: lightgrey;
            }
        td {
            background-color: white;
            color: black;
            border: 1px solid black;
        text-align: center;
        }
    </style>