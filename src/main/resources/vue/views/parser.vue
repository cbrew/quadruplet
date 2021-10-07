<template id="parser">
    <div>
    <h1 class="parser">{{name}}</h1>
        <input v-model="message" placeholder="edit me">
        <p>Message is: {{ message }}</p>
        <p>Words are: {{ words }}</p>
        <h2>Counter</h2>
        <p>{{ counter }}</p>
        <button v-on:click="addToCounter">Increment</button>
<!--
        <ul class="edges">
            <li v-for="edge in edges">
                {{edge.start}}-{{edge.label}}-{{edge.end}} <br/>
            </li>
        </ul>
        <div id="example">
            <button v-on:click="layout">Layout</button>
        </div>

        <table class="presult">
            <tr v-for="layer in layers">
                <td v-for="span in layer" :colspan="span.end - span.start" v-on:click="show">{{span.label}}</td>
            </tr>
            <tr><td class="word" v-for="word in words">{{word}}</td></tr>
        </table>
  -->
        </div>
    </template>
    <script>
        var p = Vue.component("hello-world", {

        template: "#parser",
        data: () => ({
            message: "I want an umbrella",
            counter: "",
            name: "Rich NLU",
            layers: []
        }),

        created(){


            // obtain the edges from the back end.
            fetch("/edges")
            .then(response => response.json())
            .then(data => (this.edges = data));

        },




    computed: {
        words: function () { return this.message.split(' ') },
        edges: function () {
            let v = []
            fetch("/edges")
            .then(response => response.json())
            .then(data => (v = data));
            return v;
        }

    },

    methods: {
        show: function(event) {
            alert(event.target)
        },

        getCounter:  function () {
            let v = "xxx";
            fetch("/counter").then(response => response.json()).then(data => this.counter = data)

            },
        addToCounter: function() {
            fetch("/counter", {method: "PUT"})
        },
        layout: function() {
            this.layers = []
            let layers = this.layers
                function compare(a, b) {
                    if(a.start < b.start) {
                        return -1;
                    }
                    if(b.start < a.start) {
                        return 1;
                    }

                    return a < b ? -1: 1

                }


                function overlaps(e1, layer) {
                    for(const e2 of layer) {
                        if(! (e1.start >= e2.end || e2.start >= e1.end)) {
                            return true
                        }
                    }
                    return false
                }

                function addNoOverlap(e) {
                    for(layer of layers) {
                        if (!overlaps(e, layer)) {
                            layer.push(e)
                        return
                        }
                    }
                    layers.push([e])
                }

                function addGaps() {
                    new_layers = []
                    for(layer of layers) {
                        new_layer = []
                        pos = 0
                        for(span of layer) {
                            if(span.start > pos){
                                new_layer.push({"label":"","start":pos,"end":span.start})
                            }
                            new_layer.push(span)
                            pos = span.end
                        }
                       new_layers.push(new_layer)
                    }

                    return new_layers

                }

                this.edges.forEach(addNoOverlap)
                for(layer of layers) {
                    layer.sort(compare)
                }
                this.layers = addGaps()



            }

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
         .word {
            color: blue;
            background-color: white;
            border: 1px dotted blue;
            }
        td {
            background-color: white;
            color: black;
            border: 1px solid black;
        text-align: center;
        }
        .presult {
            min-width: 300px;
            width: 100%;
         }
    </style>